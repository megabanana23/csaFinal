
import javax.swing.*;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;

public class Main {

    /**
     * Searches the CSV file for a record matching the given license plate.
     * Reads line-by-line and splits on commas to preserve empty fields.
     */
    public static void readRecord(String searchTerm, String filePath) {
        boolean found = false;

        // Variables for the matched record
        String crimeCode = "";
        String crimeDesc = "";
        String sex = "";
        String weaponCode = "";
        String licensePlate = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            br.readLine(); // Skip the header row

            String line;
            while ((line = br.readLine()) != null && !found) {
                line = line.trim();
                if (line.equals("")) continue;

                String[] fields = line.split(",", -1);
                if (fields.length < 10) continue;

                crimeCode = fields[0].trim();
                crimeDesc = fields[1].trim();
                // fields[2] = moCodes
                // fields[3] = age
                sex = fields[4].trim();
                licensePlate = fields[5].trim();
                // fields[6] = premiseCode
                // fields[7] = premiseDesc
                weaponCode = fields[8].trim();
                // fields[9] = weaponDesc

                if (licensePlate.equalsIgnoreCase(searchTerm)) {
                    found = true;
                }
            }
            br.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "File not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Post-loop: display result based on the found flag
        if (found) {
            // Convert M/F to MALE/FEMALE
            String driver = "MALE";
            if (sex.equals("F")) {
                driver = "FEMALE";
            }

            String report = "License plate: " + licensePlate
                    + "\nDRIVER: " + driver
                    + "\nWITH FOLLOWING WARRANT:"
                    + "\nCODE: " + crimeCode + " (" + crimeDesc + ")";

            // If a weapon was used, flag as dangerous
            if (!weaponCode.equals("0")) {
                report += "\nDANGEROUS";
            }

            JOptionPane.showMessageDialog(null, report, "With Warrant", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                    "License plate: " + searchTerm + "\nNO WARRANT",
                    "Without Warrant", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        String filePath = "CrimeData2.csv";

        // --- Ask if they want to search by premise code ---
        int askPremise = JOptionPane.showConfirmDialog(null,
                "Would you like to enter a premise code?",
                "Question", JOptionPane.YES_NO_OPTION);

        if (askPremise == JOptionPane.YES_OPTION) {
            String code = JOptionPane.showInputDialog(null, "Enter a premise code:");
            if (code == null) {
                System.exit(0);
            }
            code = code.trim();

            int warrantCount = 0;
            String warrantList = "";

            try {
                BufferedReader br = new BufferedReader(new FileReader(filePath));
                br.readLine(); // Skip header

                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.equals("")) continue;

                    String[] fields = line.split(",", -1);
                    if (fields.length < 10) continue;

                    String foundPlate = fields[5].trim();
                    String pCode = fields[6].trim();

                    if (code.equals(pCode)) {
                        warrantCount++;
                        warrantList += "Warrant " + warrantCount + " - " + foundPlate + "\n";
                    }
                }
                br.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "File not found!", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            if (warrantCount == 0) {
                // No warrants for this premise code
                JOptionPane.showMessageDialog(null,
                        "Premise Code: " + code + "\nNO LOCAL WARRANTS",
                        "Without Warrants", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Show warrant list, then prompt for a plate to get details
                String premiseReport = "Premise Code: " + code + "\n"
                        + warrantCount + " Local Warrants:\n" + warrantList;

                String plate = JOptionPane.showInputDialog(premiseReport
                        + "\nPlease enter license plate of warrant for details:");

                if (plate != null && !plate.trim().equals("")) {
                    plate = plate.trim().toUpperCase();
                    readRecord(plate, filePath);
                }
            }
            System.exit(0);
        }

        // --- Part 1: Direct license plate search ---
        String plate = "";
        boolean validPlate = false;

        while (!validPlate) {
            plate = JOptionPane.showInputDialog("Enter a license plate:");

            if (plate == null) {
                System.exit(0);
            }

            plate = plate.trim();

            // Easter egg
            if (plate.equalsIgnoreCase("csa")) {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=f2bHoTUiMpI"));
                } catch (Exception e) {
                    // ignore
                }
                System.exit(0);
            }

            if (plate.equals("")) {
                JOptionPane.showMessageDialog(null,
                        "You cannot have a blank license plate!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else if (plate.length() != 7 || plate.charAt(3) != '-') {
                JOptionPane.showMessageDialog(null,
                        "Invalid license plate!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                plate = plate.toUpperCase();
                validPlate = true;
            }
        }

        // Search for the plate
        readRecord(plate, filePath);
    }
}
