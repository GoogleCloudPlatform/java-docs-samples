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

public class Main {

    private static final int MAX_GENERATED_ENTRIES = 1000;

    public static void main(String[] args) {
        // Connect to PostgreSQL
        System.out.println("Connecting to PostgreSQL...");
        JdbcTemplate jdbcTemplate = configureJdbcTemplate();

        // Generate maximum score
        int maxScore = new Random().nextInt(9990) + 10;

        // Populate leaderboard with test data
        try {
            System.out.println("Populating leaderboard with test data...");
            populateLeaderboard(jdbcTemplate, maxScore);
        } catch (CannotGetJdbcConnectionException e) {
            System.out.println("Failed to connect to the database. Retrying in 5 seconds...");
            // Sleep for 5 seconds and retry
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            main(args);
        }
    }

    private static JdbcTemplate configureJdbcTemplate() {
        String jdbcUrl =
                System.getenv()
                        .getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/leaderboard");
        String jdbcUsername = System.getenv().getOrDefault("DB_USERNAME", "root");
        String jdbcPassword = System.getenv().getOrDefault("DB_PASSWORD", "password");

        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(
                DataSourceBuilder.create()
                        .url(jdbcUrl)
                        .username(jdbcUsername)
                        .password(jdbcPassword)
                        .build());
        return jdbcTemplate;
    }

    private static void populateLeaderboard(JdbcTemplate jdbcTemplate, int maxScore) {
        String sql = "INSERT INTO leaderboard (username, score) VALUES (?, ?)";

        // Prepare batch arguments
        HashSet<String> usernames = new HashSet<>();
        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = 0; i < MAX_GENERATED_ENTRIES; i++) {
            String username = generateGamingUsername();
            int score = generateSkewedScore(maxScore) * 100;

            if (usernames.contains(username)) {
                continue;
            }

            usernames.add(username);
            batchArgs.add(new Object[] {username, score});
        }

        // Execute batch update
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private static int generateSkewedScore(int maxScore) {
        Random random = new Random();
        double uniformRandom = random.nextDouble();
        double skewedRandom = Math.pow(uniformRandom, 3); // Adjust exponent for skew intensity
        return (int) (skewedRandom * maxScore);
    }

    private static String generateGamingUsername() {
        String[] adjectives = {
            "Stealthy",
            "Furious",
            "Savage",
            "Shadow",
            "Mystic",
            "Raging",
            "Epic",
            "Silent",
            "Wicked",
            "Legendary",
            "Fiery",
            "Brave",
            "Cunning",
            "Swift",
            "Lone",
            "Dark",
            "Deadly",
            "Fearless",
            "Iron",
            "Crimson",
            "Golden",
            "Silver",
            "Vengeful",
            "Thundering",
            "Frozen",
            "Venomous",
            "Infernal",
            "Glorious",
            "Hidden",
            "Mighty",
            "Proud",
            "Reckless",
            "Stalwart",
            "Fierce",
            "Grim",
            "Bold",
            "Daring",
            "Noble",
            "Heroic",
            "Vicious",
            "Stormy",
            "Merciless",
            "Ghostly",
            "Shifty",
            "Eternal",
            "Arcane",
            "Primordial",
            "Sinister",
            "Radiant",
            "Powerful",
            "Holy",
            "Unholy",
            "Blazing",
            "Ruthless",
            "Jagged",
            "Shattered",
            "Swift",
            "Haunted",
            "Gilded",
            "Crazed",
            "Savage",
            "Ravenous",
            "Lethal",
            "Frosty",
            "Spectral",
            "Divine",
            "Oblivious",
            "Venomous",
            "Twisted",
            "Blighted",
            "Howling",
            "Burning",
            "Enchanted",
            "War-torn",
            "Resilient",
            "Dominant",
            "Immortal",
            "Dire",
            "Relentless",
            "Forsaken",
            "Avenging",
            "Piercing",
            "Brutal",
            "Shimmering",
            "Dauntless",
            "Nightmarish",
            "Ferocious",
            "Infernal",
        };

        String[] nouns = {
            "Warrior",
            "Sniper",
            "Assassin",
            "Mage",
            "Knight",
            "Hunter",
            "Rogue",
            "Beast",
            "Dragon",
            "Phantom",
            "Champion",
            "Sorcerer",
            "Guardian",
            "Warlord",
            "Reaper",
            "Shinobi",
            "Valkyrie",
            "Gladiator",
            "Raider",
            "Paladin",
            "Berserker",
            "Ranger",
            "Slayer",
            "Monk",
            "Overlord",
            "Druid",
            "Invoker",
            "Barbarian",
            "Necromancer",
            "Ravager",
            "Demon",
            "Titan",
            "Shadow",
            "Wolf",
            "Leviathan",
            "Viper",
            "Hawk",
            "Phoenix",
            "Tiger",
            "Lion",
            "Serpent",
            "Ghost",
            "Predator",
            "Juggernaut",
            "Crusher",
            "Tyrant",
            "Destroyer",
            "Seer",
            "Summoner",
            "Conqueror",
            "Wanderer",
            "Blight",
            "Tempest",
            "Inferno",
            "Havoc",
            "Specter",
            "Shade",
            "Nightmare",
            "Scourge",
            "Cleric",
            "Lancer",
            "Invoker",
            "Vortex",
            "Anomaly",
            "Havoc",
            "Harbinger",
            "Sentinel",
            "Oracle",
            "Mystic",
            "Vanquisher",
            "Sage",
            "Eclipse",
            "Abyss",
            "Legion",
            "Storm",
            "Fury",
            "Outlaw",
            "Enigma",
            "Reckoning",
            "Executor",
            "Dominion",
            "Omen",
            "Fate",
            "Rift",
            "Chaos",
            "Nemesis",
            "Feral",
            "Inferno",
            "Apex",
            "Ascendant",
        };

        Random random = new Random();
        String adjective = adjectives[random.nextInt(adjectives.length)];
        String noun = nouns[random.nextInt(nouns.length)];
        String randomNumber =
                random.nextDouble() < 0.1 ? String.format("%03d", random.nextInt(100)) : "";

        String username = adjective + noun + randomNumber;
        username = applyLeetSpeak(username, random);
        return applyRandomFormatting(username, random);
    }

    private static String applyLeetSpeak(String input, Random random) {
        char[] characters = input.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            if (random.nextDouble() < 0.05) {
                switch (characters[i]) {
                    case 'a':
                    case 'A':
                        characters[i] = '4';
                        break;
                    case 'e':
                    case 'E':
                        characters[i] = '3';
                        break;
                    case 'i':
                    case 'I':
                        characters[i] = '1';
                        break;
                    case 'o':
                    case 'O':
                        characters[i] = '0';
                        break;
                    case 's':
                    case 'S':
                        characters[i] = '5';
                        break;
                }
            }
        }
        return new String(characters);
    }

    private static String applyRandomFormatting(String input, Random random) {
        int choice = random.nextInt(3);
        switch (choice) {
            case 0:
                return input.toLowerCase();
            case 1:
                return input.toUpperCase();
            case 2:
                return input.replaceAll("([a-zA-Z])(?=[A-Z])", "$1_");
            default:
                return input;
        }
    }
}
