package de.unierlangen.med.imi.allensparql;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CSVTool {

    String file = "";

    public CSVTool(String file) {
        this.file = file;
    }

    public String readCell(int row, int column) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int atRow = 1;
            while ((line = br.readLine()) != null) {
                if (atRow == row) {
                    String[] columnData = line.split(";");
                    if (columnData.length >= column - 1) {
                        return columnData[column - 1];
                    }
                }
                atRow++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
