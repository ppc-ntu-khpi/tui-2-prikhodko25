package com.mybank.tui;

import com.mybank.data.DataSource;
import com.mybank.domain.Bank;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.Customer;
import com.mybank.domain.SavingsAccount;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.jline.reader.*;
import org.jline.reader.impl.completer.*;
import org.jline.utils.*;
import org.fusesource.jansi.*;

public class CLIdemo {

    public static final String ANSI_RESET  = "\u001B[0m";
    public static final String ANSI_RED    = "\u001B[31m";
    public static final String ANSI_GREEN  = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN   = "\u001B[36m";
    public static final String ANSI_WHITE  = "\u001B[37m";

    private String[] commandsList;

    public void init() {
        commandsList = new String[]{"help", "customers", "customer", "report", "exit"};
    }

    public void run() {
        AnsiConsole.systemInstall();
        printWelcomeMessage();

        LineReaderBuilder readerBuilder = LineReaderBuilder.builder();
        List<Completer> completors = new LinkedList<>();
        completors.add(new StringsCompleter(commandsList));
        readerBuilder.completer(new ArgumentCompleter(completors));

        LineReader reader = readerBuilder.build();
        PrintWriter out = new PrintWriter(System.out);
        String line;

        while ((line = readLine(reader, "")) != null) {

            if ("help".equals(line)) {
                printHelp();

            } else if ("customers".equals(line)) {
                printCustomerList();

            } else if (line.startsWith("customer")) {
                printCustomerDetails(line);

            } else if ("report".equals(line)) {
                printReport();

            } else if ("exit".equals(line)) {
                System.out.println("Exiting application");
                return;

            } else {
                System.out.println(ANSI_RED
                    + "Invalid command. Press TAB or type \"help\" then hit ENTER."
                    + ANSI_RESET);
            }
        }

        AnsiConsole.systemUninstall();
    }

    private void printCustomerList() {
        AttributedStringBuilder a = new AttributedStringBuilder()
            .append("\nThis is all of your ")
            .append("customers", AttributedStyle.BOLD.foreground(AttributedStyle.RED))
            .append(":");
        System.out.println(a.toAnsi());

        Bank bank = Bank.getBank();
        if (bank.getNumberOfCustomers() > 0) {
            System.out.println("\nLast name\tFirst Name\tBalance");
            System.out.println("---------------------------------------");
            for (int i = 0; i < bank.getNumberOfCustomers(); i++) {
                Customer c = bank.getCustomer(i);
                System.out.printf("%s\t\t%s\t\t$%.2f%n",
                    c.getLastName(), c.getFirstName(),
                    c.getAccount(0).getBalance());
            }
        } else {
            System.out.println(ANSI_RED + "Your bank has no customers!" + ANSI_RESET);
        }
    }

    private void printCustomerDetails(String line) {
        try {
            int custNo = 0;
            if (line.length() > 8) {
                String strNum = line.split(" ")[1];
                if (strNum != null) {
                    custNo = Integer.parseInt(strNum.trim());
                }
            }

            Bank bank = Bank.getBank();
            if (custNo < 0 || custNo >= bank.getNumberOfCustomers()) {
                System.out.println(ANSI_RED + "ERROR! Wrong customer number!" + ANSI_RESET);
                return;
            }

            Customer cust = bank.getCustomer(custNo);

            AttributedStringBuilder a = new AttributedStringBuilder()
                .append("\nDetailed information about customer #")
                .append(Integer.toString(custNo), AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                .append(":");
            System.out.println(a.toAnsi());

            System.out.println("\nLast name\tFirst Name\tAccount Type\tBalance");
            System.out.println("-------------------------------------------------------");

            for (int i = 0; i < cust.getNumberOfAccounts(); i++) {
                String accType = cust.getAccount(i) instanceof CheckingAccount
                    ? "Checking" : "Savings";
                System.out.printf("%s\t\t%s\t\t%s\t$%.2f%n",
                    cust.getLastName(), cust.getFirstName(),
                    accType, cust.getAccount(i).getBalance());
            }

        } catch (Exception e) {
            System.out.println(ANSI_RED + "ERROR! Wrong customer number!" + ANSI_RESET);
        }
    }

    private void printReport() {
        Bank bank = Bank.getBank();

        AttributedStringBuilder a = new AttributedStringBuilder()
            .append("\nCUSTOMER REPORT",
                AttributedStyle.BOLD.foreground(AttributedStyle.CYAN));
        System.out.println(a.toAnsi());
        System.out.println("=".repeat(55));
        System.out.printf("%-5s %-15s %-12s %10s%n",
            "No.", "Name", "Acct Type", "Balance");
        System.out.println("-".repeat(55));

        for (int i = 0; i < bank.getNumberOfCustomers(); i++) {
            Customer cust = bank.getCustomer(i);
            String fullName = cust.getFirstName() + " " + cust.getLastName();

            for (int j = 0; j < cust.getNumberOfAccounts(); j++) {
                String accType = cust.getAccount(j) instanceof CheckingAccount
                    ? "Checking" : "Savings";
                double balance = cust.getAccount(j).getBalance();

                String balStr = (balance > 0)
                    ? ANSI_GREEN + String.format("$%10.2f", balance) + ANSI_RESET
                    : ANSI_RED   + String.format("$%10.2f", balance) + ANSI_RESET;

                System.out.printf("%-5d %-15s %-12s %s%n",
                    i, (j == 0 ? fullName : ""), accType, balStr);
            }
        }
        System.out.println("=".repeat(55));
    }

    private void printWelcomeMessage() {
        System.out.println("\nWelcome to " + ANSI_GREEN + " MyBank Console Client App" + ANSI_RESET
            + "!\nFor assistance press TAB or type \"help\" then hit ENTER.");
    }

    private void printHelp() {
        System.out.println("help\t\t\t- Show help");
        System.out.println("customers\t\t- Show list of customers");
        System.out.println("customer 'index'\t- Show customer details");
        System.out.println("report\t\t\t- Show full customer report");
        System.out.println("exit\t\t\t- Exit the app");
    }

    private String readLine(LineReader reader, String promptMessage) {
        try {
            String line = reader.readLine(promptMessage + ANSI_YELLOW + "\nbank> " + ANSI_RESET);
            return line.trim();
        } catch (UserInterruptException e) {
            return null;
        } catch (EndOfFileException e) {
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);

        DataSource ds = new DataSource("data/test.dat");
        ds.loadData();

        CLIdemo shell = new CLIdemo();
        shell.init();
        shell.run();
    }
}
