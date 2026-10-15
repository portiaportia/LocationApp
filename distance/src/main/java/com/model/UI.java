package com.model;

import com.location.Address;
import com.location.MapTools;

public class UI {

    public void run() {
        // A Charleston address near the waterfront
        Address userAddress = new Address(
                "1 Vendue Range",
                "Charleston",
                "SC",
                "29401"
        );
        Address closestShelter;

        // Example hurricane-shelter locations
        Address[] shelters = {
                new Address("2950 Carner Avenue",
                        "North Charleston", "SC", "29405"), // Military Magnet

                new Address("5200 Lackawanna Boulevard",
                        "North Charleston", "SC", "29405"), // North Charleston CAES

                new Address("3841 Leeds Avenue",
                        "North Charleston", "SC", "29405"), // Bond Court

                new Address("3625 Ashley Phosphate Road",
                        "North Charleston", "SC", "29418"), // Stall High School

                new Address("6401 Dorchester Road",
                        "North Charleston", "SC", "29418"), // Zucker Middle School

                new Address("1624 State Road",
                        "Summerville", "SC", "29483"), // Cane Bay High School

                new Address("1053 Russellville Road",
                        "St. Stephen", "SC", "29479"), // St. Stephen Elementary

                new Address("200 Macedonia Foxes Circle",
                        "St. Stephen", "SC", "29479"), // Macedonia Middle School

                new Address("101 Westview Boulevard",
                        "Goose Creek", "SC", "29445"), // Westview Middle School

                new Address("1101 Boone Hill Road",
                        "Summerville", "SC", "29483") // Summerville High School
        };

        try {
            closestShelter = MapTools.getInstance()
                    .findClosestAddress(userAddress, shelters);

            System.out.println("User address: "
                    + userAddress);

            System.out.println("Closest shelter: "
                    + closestShelter);

            String[] directions = MapTools.getInstance().getDrivingDirections(userAddress, closestShelter);

            for (String direction : directions) {
                System.out.println(direction);
            }

        } catch (Exception e) {
            System.out.println("There was a problem finding a shelter.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new UI().run();
    }
}