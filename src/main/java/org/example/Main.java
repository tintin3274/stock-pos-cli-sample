package org.example;

import java.io.*;
import java.util.*;

public class Main {
    private static final String FS = System.getProperty("file.separator");
    private static final String HEADER_STOCK_CSV = "id,name,price,quantity";
    private static final String HEADER_REPORT_CSV = "timestamp,id,name,price,quantity,total";
    private static final List<Item> ITEM_LIST = new ArrayList<>();
    private static final Map<String, Item> ITEM_MAP = new HashMap<>();

    public static void main(String[] args) {
        try {
            File directoryData = new File("data");
            if (!directoryData.exists()) {
                directoryData.mkdirs();
            }

            File fileStock = new File("data"+FS+"stock.csv");
            if (!fileStock.exists()) {
                writeStock();
            }
            else {
                readStock();
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            menuConsole();
            while (true) {
                String mode = bufferedReader.readLine();
                switch (mode.toUpperCase()) {
                    case "L":
                        showListItem();
                        break;
                    case "S":
                        saleItem(bufferedReader);
                        break;
                    case "C":
                        clearConsole();
                        menuConsole();
                        break;
                    case "E":
                        System.exit(1);
                        break;
                    default:
                        System.err.println("=== Wrong mode operation ===");
                }
            }

        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final static void menuConsole() {
        System.out.println("=== WELCOME TO POS ===");
        System.out.println("<L> ITEM LIST");
        System.out.println("<S> SALE ITEM");
        System.out.println("<C> CLEAR CONSOLE");
        System.out.println("<E> EXIT PROGRAM");
        System.out.println("======================");
    }

    public final static void showListItem() {
        System.out.println("=== ITEM LIST ===");
        for (Item item : ITEM_LIST) {
            System.out.println(item);
        }
        System.out.println("=================");
    }

    public final static void saleItem(BufferedReader bufferedReader) throws IOException {
        System.out.println("=== SALE ITEM ===");
        System.out.println("Format: [ID] [QUANTITY]");
        System.out.println("End: 0");
        System.out.println("*****************");

        String input;
        List<Report> reportList = new ArrayList<>();
        while ((input = bufferedReader.readLine()) != null) {
            if (input.equals("0")) {
                break;
            }
            String[] strings = input.split(" ");
            if (strings.length != 2) {
                System.err.println("Error: Invalid input format.");
            }
            else {
                String id = strings[0];
                int quantity;
                try {
                    quantity = Integer.parseInt(strings[1]);
                    Report report = new Report();
                    report.setId(id);
                    report.setQuantity(quantity);
                    reportList.add(report);
                }
                catch (NumberFormatException e) {
                    System.err.println("Error: Invalid input format.");
                }
            }
        }

        writeReport(reportList);
        System.out.println("=================");
    }

    public final static void readStock() throws IOException {
        File fileStock = new File("data"+FS+"stock.csv");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileStock));
        String header = bufferedReader.readLine();

        if (!header.equals(HEADER_STOCK_CSV)) {
            closeProgramByError(new Exception("Header file not match."));
        }

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] strings = line.split(",");
            String id = strings[0];
            String name = strings[1];
            double price = Double.parseDouble(strings[2]);
            int quantity = Integer.parseInt(strings[3]);
            Item item = new Item(id, name, price, quantity);
            ITEM_LIST.add(item);
            ITEM_MAP.put(id, item);
        }
    }

    public final static void writeStock() throws IOException {
        File fileStock = new File("data"+FS+"stock.csv");
        if (!fileStock.exists()) {
            fileStock.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(fileStock);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(HEADER_STOCK_CSV);

        for (Item item : ITEM_LIST) {
            bufferedWriter.newLine();
            bufferedWriter.write(item.getId()+","+item.getName()+","+item.getPrice()+","+item.getQuantity());
        }
        bufferedWriter.close();
    }

    public final static void writeReport(List<Report> reportList) {
        try {
            File fileReport = new File("data"+FS+"report.csv");
            BufferedWriter bufferedWriter;
            if (!fileReport.exists()) {
                fileReport.createNewFile();

                FileWriter fileWriter = new FileWriter(fileReport);
                bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(HEADER_REPORT_CSV);
            }
            else {
                FileWriter fileWriter = new FileWriter(fileReport, true);
                bufferedWriter = new BufferedWriter(fileWriter);
            }

            Date date = new Date();
            StringBuilder stringBuilder = new StringBuilder();
            for (Report report : reportList) {
                if (ITEM_MAP.containsKey(report.getId())) {
                    Item item = ITEM_MAP.get(report.getId());
                    if (item.getQuantity() > 0 && item.getQuantity() >= report.getQuantity()) {
                        item.setQuantity(item.getQuantity() - report.getQuantity());

                        report.setDate(date);
                        report.setName(item.getName());
                        report.setPrice(item.getPrice());
                        report.setTotal(item.getPrice() * report.getQuantity());

                        stringBuilder.append("\n")
                                .append(report.getDate().toString())
                                .append(",").append(report.getId())
                                .append(",").append(report.getName())
                                .append(",").append(report.getPrice())
                                .append(",").append(report.getQuantity())
                                .append(",").append(report.getTotal());
                    }
                }
            }

            if (stringBuilder.length() > 0) {
                bufferedWriter.write(stringBuilder.toString());
            }

            bufferedWriter.close();
            writeStock();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    public final static void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            }
            else {
                System.out.print("\033\143");
            }
        } catch (IOException | InterruptedException e) {
            try {
                System.err.println("Error: Can't clear screen. System close program.");
                waitEnter();
            }
            catch (IOException ioException) {
            }
            finally {
                System.exit(1);
            }

        }
    }

    public final static void waitEnter() throws IOException {
        System.out.println("=== <Enter> To continue. ===");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        bufferedReader.readLine();
    }

    public final static void closeProgramByError(Exception e) {
        try {
            System.err.println("=== System close program because error. ===");
            if (e != null) {
                System.err.println("Error: " + e.getMessage());
            }
            waitEnter();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            System.exit(1);
        }
    }
}