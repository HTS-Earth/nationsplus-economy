package com.ollethunberg;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.ollethunberg.commands.CommandBalance;
import com.ollethunberg.commands.CommandBank;
import com.ollethunberg.commands.CommandBankManager;
import com.ollethunberg.commands.CommandPay;

public class CommandHandler implements CommandExecutor {

    // This method is called, when somebody uses our command
    Connection conn;
    Plugin plugin = NationsPlusEconomy.getPlugin(NationsPlusEconomy.class);

    public CommandHandler(Connection _connection) {
        conn = _connection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName();
        if (sender instanceof Player) {

            Player executor = (Player) sender;
            if (cmd.equalsIgnoreCase("balance")) {
                if (args.length == 0) {
                    new CommandBalance(conn).balance(executor, executor.getName());
                    return true;
                } else if (args.length == 1) {
                    if (args[0] != null) {
                        new CommandBalance(conn).balance(executor, args[0]);
                        return true;
                    }

                } else if (args[1].equalsIgnoreCase("give")) {
                    // args[0] = target
                    // args[1] = give
                    // args[2] = amount
                    if (args[0] != null && args[2] != null) {
                        new CommandBalance(conn).give(executor, args[0], Float.parseFloat(args[2]));
                        return true;
                    }
                }
            } else if (cmd.equalsIgnoreCase("pay")) {
                if (args.length == 2) {
                    if (args[0] != null && args[1] != null) {
                        new CommandPay(conn).pay(executor, args[0], Float.parseFloat(args[1]));
                        return true;
                    }
                }
            } else if (cmd.equalsIgnoreCase("bank")) {
                if (args.length == 0) {
                    try {
                        new CommandBank(conn).bankAccount(executor);
                    } catch (SQLException e) {
                        executor.sendMessage("§cAn error occured when trying to find your bank account!");
                        e.printStackTrace();
                    }

                    return true;
                }
                String action = args[0];

                if (action.equalsIgnoreCase("create")) {

                    String bankName = args[1];
                    if (bankName == null) {
                        executor.sendMessage("§cYou need to specify a bank name!");
                        return true;
                    }
                    try {
                        new CommandBank(conn).createBank(executor, bankName);
                        return true;
                    } catch (SQLException e) {
                        // give the error to the user
                        executor.sendMessage("§cAn error occured while creating the bank!");
                        e.printStackTrace();
                        return false;
                    }

                } else if (action.equalsIgnoreCase("account")) {
                    if (args.length == 1) {
                        executor.sendMessage("§cYou need to provide more arguments!");
                        return true;
                    }
                    String accountAction = args[1];
                    if (accountAction.equalsIgnoreCase("open")) {
                        String bankName = args[2];
                        if (bankName == null) {
                            executor.sendMessage("§cYou need to provide a bank name!");
                            return true;
                        }
                        try {
                            new CommandBank(conn).openBankAccount(executor, bankName);
                            return true;
                        } catch (SQLException e) {
                            executor.sendMessage("§cAn error occured while opening the bank account!");
                            e.printStackTrace();
                            return false;
                        }
                    } else if (accountAction.equalsIgnoreCase("close")) {
                        try {
                            new CommandBank(conn).closeBankAccount(executor);
                            return true;
                        } catch (SQLException e) {
                            executor.sendMessage("§cAn error occured while closing the bank account!");
                            e.printStackTrace();
                            return false;
                        }
                    }

                } else if (action.equalsIgnoreCase("deposit")) {
                    if (args.length == 1) {
                        executor.sendMessage("§cYou need to provide an amount!");
                        return true;
                    }
                    // deposit into the bank
                    Float amount = Float.parseFloat(args[1]);
                    if (amount <= 0) {
                        executor.sendMessage("§cYou can't deposit a negative amount!");
                        return true;
                    }
                    try {
                        new CommandBank(conn).deposit(executor, amount);
                        return true;
                    } catch (SQLException e) {
                        executor.sendMessage("§cAn error occured while depositing into the bank!");
                        e.printStackTrace();
                        return true;
                    }
                } else if (action.equalsIgnoreCase("withdraw")) {
                    if (args.length == 1) {
                        executor.sendMessage("§cYou need to provide an amount!");
                        return true;
                    }
                    // withdraw from the bank
                    Float amount = Float.parseFloat(args[1]);

                    try {
                        new CommandBank(conn).withdraw(executor, amount);
                        return true;
                    } catch (SQLException e) {
                        executor.sendMessage("§cAn error occured while withdrawing from the bank!");
                        e.printStackTrace();
                        return true;
                    }

                } else {
                    executor.sendMessage("§cUnknown action!");
                    return true;
                }

            } else if (cmd.equalsIgnoreCase("banks")) {
                try {
                    new CommandBank(conn).listBanks(executor);
                    return true;
                } catch (SQLException e) {
                    // give the error to the user
                    executor.sendMessage("§cAn error occured while listing the banks!");
                    e.printStackTrace();
                    return false;
                }
            } else if (cmd.equalsIgnoreCase("bankmanager")) {
                if (args.length == 0) {
                    executor.sendMessage("Available commands: ");
                    executor.sendMessage(
                            "§c/bankmanager interest [amount in %]" + "§7 - Set the interest rate for the bank");
                    executor.sendMessage("§c/bankmanager interest" + "§7 - Get the interest rate for the bank");
                    // see requested loans and accept or deny them
                    executor.sendMessage("§c/bankmanager loans" + "§7 - See the requested loans");
                    executor.sendMessage("§c/bankmanager loans accept [id]" + "§7 - Accept a loan");
                    executor.sendMessage("§c/bankmanager loans deny [id]" + "§7 - Deny a loan");
                    return true;
                }
                String action = args[0];
                if (action.equalsIgnoreCase("interest")) {
                    if (args.length == 1) {
                        try {
                            new CommandBankManager(conn).getInterest(executor);
                            return true;
                        } catch (SQLException e) {
                            executor.sendMessage("§cAn error occured while getting the interest rate!");
                            e.printStackTrace();
                            return true;
                        }
                    } else if (args.length == 2) {
                        Float interest = Float.parseFloat(args[1]) / 100;
                        try {
                            new CommandBankManager(conn).setInterest(executor, interest);
                            return true;
                        } catch (SQLException e) {
                            executor.sendMessage("§cAn error occured while setting the interest rate!");
                            e.printStackTrace();
                            return true;
                        }
                    }
                } else if (action.equalsIgnoreCase("loans")) {
                    if (args.length == 1) {
                        try {
                            new CommandBankManager(conn).listLoans(executor, true, true);
                            return true;
                        } catch (SQLException e) {
                            executor.sendMessage("§cAn error occured while listing the loans!");
                            e.printStackTrace();
                            return true;
                        }
                    } else if (args.length == 2 && args[1].equalsIgnoreCase("offers")) {
                        try {
                            new CommandBankManager(conn).listLoans(executor, false, false);
                            return true;
                        } catch (SQLException e) {
                            executor.sendMessage("§cAn error occured while listing the loans!");
                            e.printStackTrace();
                            return true;
                        }
                    }

                    else if (args.length == 3) {
                        String loanAction = args[1];
                        if (loanAction.equalsIgnoreCase("accept")) {
                            int id = Integer.parseInt(args[2]);
                            try {
                                new CommandBankManager(conn).updateOfferStatus(executor, id, true);
                                return true;
                            } catch (SQLException e) {
                                executor.sendMessage("§cAn error occured while accepting the loan!");
                                e.printStackTrace();
                                return true;
                            }
                        } else if (loanAction.equalsIgnoreCase("deny")) {
                            int id = Integer.parseInt(args[2]);
                            try {
                                new CommandBankManager(conn).updateOfferStatus(executor, id, false);
                                return true;
                            } catch (SQLException e) {
                                executor.sendMessage("§cAn error occured while denying the loan!");
                                e.printStackTrace();
                                return true;
                            }
                        }
                    }
                }

            }

        }

        return false;
    }
}
