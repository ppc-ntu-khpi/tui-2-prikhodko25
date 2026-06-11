package com.mybank.tui;

import com.mybank.data.DataSource;
import com.mybank.domain.Account;
import com.mybank.domain.Bank;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.Customer;
import com.mybank.domain.SavingsAccount;
import java.util.Locale;

import jexer.TAction;
import jexer.TApplication;
import jexer.TField;
import jexer.TText;
import jexer.TWindow;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;

public class TUIdemo extends TApplication {

    private static final int ABOUT_APP = 2000;
    private static final int CUST_INFO = 2010;
    private static final int CUST_REPORT = 2020;

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);

        DataSource ds = new DataSource("data/test.dat");
        ds.loadData();

        TUIdemo tdemo = new TUIdemo();
        (new Thread(tdemo)).start();
    }

    public TUIdemo() throws Exception {
        super(BackendType.SWING);

        addToolMenu();

        TMenu fileMenu = addMenu("&File");
        fileMenu.addItem(CUST_INFO, "&Customer Info");
        fileMenu.addItem(CUST_REPORT, "&Customer Report");
        fileMenu.addDefaultItem(TMenu.MID_SHELL);
        fileMenu.addSeparator();
        fileMenu.addDefaultItem(TMenu.MID_EXIT);

        addWindowMenu();

        TMenu helpMenu = addMenu("&Help");
        helpMenu.addItem(ABOUT_APP, "&About...");

        setFocusFollowsMouse(true);

        showCustomerDetails();
    }

    @Override
    protected boolean onMenu(TMenuEvent menu) {
        if (menu.getId() == ABOUT_APP) {
            messageBox("About",
                "\t\t\t   MyBank TUI Demo\n\n" +
                "Reads data from test.dat and displays\n" +
                "customer and account information.\n\n" +
                "Copyright \u00A9 2025 MyBank").show();
            return true;
        }
        if (menu.getId() == CUST_INFO) {
            showCustomerDetails();
            return true;
        }
        if (menu.getId() == CUST_REPORT) {
            showCustomerReport();
            return true;
        }
        return super.onMenu(menu);
    }

    private void showCustomerDetails() {
        TWindow custWin = addWindow("Customer Info", 2, 1, 50, 14, TWindow.NOZOOMBOX);
        custWin.newStatusBar("Enter customer number (0-based) and press Show...");

        Bank.getBank();
        custWin.addLabel("Customer number (0 - " +
            (Bank.getNumberOfCustomers() - 1) + "): ", 2, 2);
        TField custNo = custWin.addField(38, 2, 4, false);

        TText details = custWin.addText(
            "Enter a customer number above and\npress [Show] to view details.",
            2, 4, 46, 8);

        custWin.addButton("&Show", 40, 2, new TAction() {
            @Override
            public void DO() {
                try {
                    int custNum = Integer.parseInt(custNo.getText().trim());
                    if (custNum < 0 || custNum >= Bank.getNumberOfCustomers()) {
                        messageBox("Error",
                            "Customer number must be between 0 and " +
                            (Bank.getNumberOfCustomers() - 1) + "!").show();
                        return;
                    }

                    Customer cust = Bank.getCustomer(custNum);
                    StringBuilder sb = new StringBuilder();
                    sb.append("Name:     ").append(cust.getFirstName())
                      .append(" ").append(cust.getLastName()).append("\n");
                    sb.append("Accounts: ").append(cust.getNumberOfAccounts()).append("\n\n");

                    for (int i = 0; i < cust.getNumberOfAccounts(); i++) {
                        Account acc = cust.getAccount(i);
                        sb.append("  Account #").append(i + 1).append(": ");

                        if (acc instanceof SavingsAccount) {
                            SavingsAccount sa = (SavingsAccount) acc;
                            sb.append("Savings\n");
                            sb.append("    Balance:  $")
                              .append(String.format("%.2f", sa.getBalance())).append("\n");
                        } else if (acc instanceof CheckingAccount) {
                            sb.append("Checking\n");
                            sb.append("    Balance:  $")
                              .append(String.format("%.2f", acc.getBalance())).append("\n");
                        } else {
                            sb.append("Unknown\n");
                            sb.append("    Balance:  $")
                              .append(String.format("%.2f", acc.getBalance())).append("\n");
                        }
                    }

                    details.setText(sb.toString());

                } catch (NumberFormatException e) {
                    messageBox("Error", "Please enter a valid customer number!").show();
                }
            }
        });
    }

    private void showCustomerReport() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%-4s %-15s %-12s %10s%n",
            "#", "Name", "Acct Type", "Balance"));
        sb.append("-".repeat(45)).append("\n");

        for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
            Customer cust = Bank.getCustomer(i);
            String fullName = cust.getFirstName() + " " + cust.getLastName();

            for (int j = 0; j < cust.getNumberOfAccounts(); j++) {
                Account acc = cust.getAccount(j);
                String accType = (acc instanceof SavingsAccount) ? "Savings" : "Checking";
                sb.append(String.format("%-4d %-15s %-12s %10.2f%n",
                    i, fullName, accType, acc.getBalance()));
                fullName = "";
            }
        }

        TWindow reportWin = addWindow("Customer Report",
            4, 1, 52, 18, TWindow.NOZOOMBOX);
        reportWin.newStatusBar("All customers and accounts from test.dat");
        reportWin.addText(sb.toString(), 1, 1, 49, 14);
    }
}
