/*
 * Filename     decrypt.java
 * Date         09/28/2021
 * Author       Ryan Pearlman
 * Email        rap180002@utdallas.edu
 * Course       Data and Applications Security
 * Course Num   CS 4389.501 Fall 2021
 * Version      1.0
 * Copyright 2021, All Rights Reserved
 *
 * DESCRIPTION
 * This program runs two different decryption methods for
 * decoding ceaser cipher encrypted text. A brute force method,
 * and a Phi Value Correlation method.
 * 
 * COMPILE COMMAND
 * javac decrypt.java
 * 
 * USAGE WITH INPUT FILE
 * java decrypt.java <input file>
 * 
 * USAGE WITH COMMAND LINE INPUT
 * java decrypt.java
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class decrypt {

    public static String method = "";
    public static String encryptedText = "";
    public static String decryptedText = "";
    public static Scanner sc;
    public static final String BRUTEFORCE = "BF";
    public static final String CORRELATION = "COR";
    public static final char[] ALPHABET = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    public static Map<Character, Double> englishFrequencies = new HashMap<Character, Double>();
    public static Map<Character, Double> inputFrequencies = new HashMap<Character, Double>();
    public static HashMap<Integer, Double> phiValues = new HashMap<Integer, Double>();

    public static void main(String[] args) {

        // file input mode
        if (args.length != 0) {
            try {
                File inputFile = new File(args[0]);
                sc = new Scanner(inputFile);
                method = sc.nextLine().trim().toUpperCase();
                while (sc.hasNextLine()) {
                    encryptedText += sc.nextLine().trim().toUpperCase();
                }
                sc.close();
                String methodName = method.equals(BRUTEFORCE) ? "Brute Force" : "Correlation";
                System.out.println("\nDecryption Method: " + methodName);
                System.out.println("Encrpyted Text: " + encryptedText + "\n");
            } catch (FileNotFoundException e) {
                System.out.println("Bad Input File.");
                e.printStackTrace();

                // user input mode
            }
        } else {
            sc = new Scanner(System.in); // Create a Scanner object
            System.out.print("Decryption Method (bf = Brute Force, cor = phi correlation): ");
            method = sc.nextLine().trim().toUpperCase(); // Read user input
            System.out.print("Encrypted Text: ");
            encryptedText = sc.nextLine().trim().toUpperCase(); // Read user input
            sc.close();
        }

        if (method.equals(BRUTEFORCE)) {

            for (int i = 0; i < ALPHABET.length; i++) {
                System.out.print("Shift " + i + ": ");
                decryptedText = "";
                for (int j = 0; j < encryptedText.length(); j++) {
                    for (int k = 0; k < ALPHABET.length; k++) {
                        if (encryptedText.charAt(j) == ALPHABET[k]) {
                            decryptedText += ALPHABET[(k + i) % ALPHABET.length];
                        }
                    }
                }
                System.out.println(decryptedText);
            }

        } else if (method.equals(CORRELATION)) {
            // insert english porbabilities into map
            insertProbabilities();

            // determine frequencies for encrypted text
            for (int i = 0; i < encryptedText.length(); i++) {
                char key = encryptedText.charAt(i);
                if (!inputFrequencies.containsKey(key)) {
                    inputFrequencies.put(key, 1.0 / encryptedText.length());
                } else {
                    inputFrequencies.put(key,
                            ((inputFrequencies.get(key) * encryptedText.length()) + 1) / encryptedText.length());
                }
            }

            // print out frequency table
            System.out.println("\nCharacter Frequencies for Encrypted Text:\n");
            for (char key : inputFrequencies.keySet()) {
                Double value = inputFrequencies.get(key);
                System.out.println("[" + key + ", " + (Math.floor(value * 1000) / 1000) + "]");
            }
            System.out.println();

            // for each shift amount, preform phi analysis
            for (int i = 0; i < ALPHABET.length; i++) {
                Double phiValue = 0.0;
                for (char key : inputFrequencies.keySet()) {
                    Double inputFrequency = inputFrequencies.get(key);
                    char shiftCharacter = '!';
                    // search alphabet array for letter, then return shifted letter
                    for (int j = 0; j < ALPHABET.length; j++) {
                        if (key == ALPHABET[j]) {
                            // shiftCharacter = ALPHABET[(j - i) % ALPHABET.length];
                            shiftCharacter = ALPHABET[((((j - i) % ALPHABET.length) + ALPHABET.length)
                                    % ALPHABET.length)];

                        }
                    }
                    Double englishFrequency = englishFrequencies.get(shiftCharacter);
                    phiValue += inputFrequency * englishFrequency;

                }
                phiValues.put(i, phiValue);
            }

            Map<Integer, Double> sorted = sortByValue(phiValues);

            // print out phi value table
            System.out.println("Phi Values for Shift Amounts In Order:\n");
            for (int shift : sorted.keySet()) {
                Double value = phiValues.get(shift);
                System.out.println("[" + shift + ", " + (Math.floor(value * 10000) / 10000) + "]");
            }
            System.out.println();

            // print out possible decrypted values
            System.out.println("Possible Decrypted Text Options:\n");
            for (int shift : sorted.keySet()) {
                System.out.print("Shift " + shift + ": ");
                decryptedText = "";
                for (int j = 0; j < encryptedText.length(); j++) {
                    for (int k = 0; k < ALPHABET.length; k++) {
                        if (encryptedText.charAt(j) == ALPHABET[k]) {
                            decryptedText += ALPHABET[((((k - shift) % ALPHABET.length) + ALPHABET.length)
                                    % ALPHABET.length)];
                        }
                    }
                }
                System.out.println(decryptedText);
            }

        }

        System.out.println();

    }

    // function to sort hashmap by values
    public static HashMap<Integer, Double> sortByValue(HashMap<Integer, Double> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<Integer, Double>> list = new LinkedList<Map.Entry<Integer, Double>>(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return -1 * (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<Integer, Double> temp = new LinkedHashMap<Integer, Double>();
        for (Map.Entry<Integer, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public static int letterIndex(char letter) {
        for (int i = 0; i < ALPHABET.length; i++) {
            if (letter == ALPHABET[i])
                return i;
        }
        return -1;
    }

    public static void insertProbabilities() {
        englishFrequencies.put('A', .08);
        englishFrequencies.put('B', .015);
        englishFrequencies.put('C', .03);
        englishFrequencies.put('D', .04);
        englishFrequencies.put('E', .13);
        englishFrequencies.put('F', .02);
        englishFrequencies.put('G', .015);
        englishFrequencies.put('H', .06);
        englishFrequencies.put('I', .065);
        englishFrequencies.put('J', .005);
        englishFrequencies.put('K', .005);
        englishFrequencies.put('L', .035);
        englishFrequencies.put('M', .03);
        englishFrequencies.put('N', .07);
        englishFrequencies.put('O', .08);
        englishFrequencies.put('P', .02);
        englishFrequencies.put('Q', .002);
        englishFrequencies.put('R', .065);
        englishFrequencies.put('S', .06);
        englishFrequencies.put('T', .09);
        englishFrequencies.put('U', .03);
        englishFrequencies.put('V', .01);
        englishFrequencies.put('W', .015);
        englishFrequencies.put('X', .005);
        englishFrequencies.put('Y', .02);
        englishFrequencies.put('Z', .002);
    }
}