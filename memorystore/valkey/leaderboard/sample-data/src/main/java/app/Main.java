/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Utility class for populating a leaderboard database with test data.
 */
public final class Main {
    /** Maximum number of entries to generate for the leaderboard. */
    private static final int MAX_GENERATED_ENTRIES = 1000;
    /** Maximum offset for random score generation. */
    private static final int MAX_SCORE_OFFSET = 9990;
    /** Minimum score value. */
    private static final int MIN_SCORE = 10;
    /** Delay in milliseconds before retrying database connection. */
    private static final int RETRY_DELAY_MS = 5000;
    /** Multiplier applied to generated scores. */
    private static final int SCORE_MULTIPLIER = 100;
    /** Probability of adding a number to username. */
    private static final double USERNAME_NUMBER_PROBABILITY = 0.1;
    /** Maximum number to append to username. */
    private static final int MAX_USERNAME_NUMBER = 100;
    /** Probability of applying leetspeak to a character. */
    private static final double LEETSPEAK_PROBABILITY = 0.05;
    /** Number of formatting options for usernames. */
    private static final int FORMAT_OPTIONS = 3;
    /** Exponent used for score distribution skewing. */
    private static final double SKEW_EXPONENT = 3.0;
    /** Message format for database retry. */
    private static final String DB_RETRY_MSG = "Failed to connect"
            + " to the database. Retrying in %d seconds...%n";
    /** Scale factor for convering ms to s. */
    private static final int MS_SCALE_FACTOR = 1000;

    private Main() {
    }

    /**
     * Main method for populating the leaderboard database with test data.
     *
     * @param args command line arguments (not used)
     */
    public static void main(final String[] args) {
        System.out.println("Connecting to PostgreSQL...");
        JdbcTemplate jdbcTemplate = configureJdbcTemplate();

        int maxScore = new Random().nextInt(MAX_SCORE_OFFSET) + MIN_SCORE;

        try {
            System.out.println("Populating leaderboard with test data...");
            populateLeaderboard(jdbcTemplate, maxScore);
        } catch (CannotGetJdbcConnectionException e) {
            System.out.printf(DB_RETRY_MSG, RETRY_DELAY_MS / MS_SCALE_FACTOR);
            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            main(args);
        }
    }

    private static JdbcTemplate configureJdbcTemplate() {
        String jdbcUrl = System.getenv().getOrDefault("DB_URL",
                "jdbc:postgresql://localhost:5432/leaderboard");
        String jdbcUsername = System.getenv()
                .getOrDefault("DB_USERNAME", "root");
        String jdbcPassword = System.getenv()
                .getOrDefault("DB_PASSWORD", "password");

        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(
                DataSourceBuilder.create()
                        .url(jdbcUrl)
                        .username(jdbcUsername)
                        .password(jdbcPassword)
                        .build());
        return jdbcTemplate;
    }

    private static void populateLeaderboard(
            final JdbcTemplate jdbcTemplate, final int maxScore) {
        String sql = "INSERT INTO leaderboard (username, score) VALUES (?, ?) "
                + "ON CONFLICT (username) DO UPDATE "
                + "SET score = EXCLUDED.score";

        HashSet<String> usernames = new HashSet<>();
        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = 0; i < MAX_GENERATED_ENTRIES; i++) {
            String username = generateGamingUsername();
            int score = generateSkewedScore(maxScore) * SCORE_MULTIPLIER;

            if (usernames.contains(username)) {
                continue;
            }

            usernames.add(username);
            batchArgs.add(new Object[] {
                    username, score });
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private static String generateGamingUsername() {
        Random random = new Random();
        String adjective = WordLists.ADJECTIVES[random
                .nextInt(WordLists.ADJECTIVES.length)];
        String noun = WordLists.NOUNS[random
                .nextInt(WordLists.NOUNS.length)];
        String randomNumber = random.nextDouble() < USERNAME_NUMBER_PROBABILITY
                ? String.format("%03d", random.nextInt(MAX_USERNAME_NUMBER))
                : "";

        String username = adjective + noun + randomNumber;
        username = applyLeetSpeak(username, random);
        return applyRandomFormatting(username, random);
    }

    private static int generateSkewedScore(final int maxScore) {
        Random random = new Random();
        double uniformRandom = random.nextDouble();
        double skewedRandom = Math.pow(uniformRandom, SKEW_EXPONENT);
        return (int) (skewedRandom * maxScore);
    }

    private static String applyLeetSpeak(final String input,
            final Random random) {
        char[] characters = input.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            if (random.nextDouble() < LEETSPEAK_PROBABILITY) {
                characters[i] = switch (characters[i]) {
                    case 'a', 'A' -> '4';
                    case 'e', 'E' -> '3';
                    case 'i', 'I' -> '1';
                    case 'o', 'O' -> '0';
                    case 's', 'S' -> '5';
                    default -> characters[i];
                };
            }
        }
        return new String(characters);
    }

    private static String applyRandomFormatting(final String input,
            final Random random) {
        return switch (random.nextInt(FORMAT_OPTIONS)) {
            case 0 -> input.toLowerCase();
            case 1 -> input.toUpperCase();
            case 2 -> input.replaceAll("([a-zA-Z])(?=[A-Z])", "$1_");
            default -> input;
        };
    }
}
