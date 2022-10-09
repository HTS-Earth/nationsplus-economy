package com.ollethunberg.commands.loan.models;

public class DBLoan {
    public int id;
    public String bank_name;
    public String player_id;
    public float amount_paid;
    public float amount_total;
    public float interest_rate;
    public boolean accepted;
    public boolean active;
    public int payments_total;
    public int payments_left;
}
