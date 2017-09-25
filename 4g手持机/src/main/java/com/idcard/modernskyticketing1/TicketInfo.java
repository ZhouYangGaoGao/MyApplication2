package com.idcard.modernskyticketing1;

import java.io.Serializable;

/**
 * Created by daxu on 5/11/17.
 */

public class TicketInfo implements Serializable {

    private String customer_name;
    private int product_count;
    private int total_price;
    private String order_num;
    private String misc_info;
    private String id;



    public String getId() {
        return id;
    }

    public String getCustomer_name() {

        return customer_name;
    }

    public int getProduct_count() {

        return product_count;
    }

    public int getTotal_price() {

        return total_price;
    }

    public int getPerPrice() {

        return (total_price / product_count);
    }

    public String getOrder_num() {

        return order_num;
    }

    public String getMisc_info() {

        return misc_info;
    }


    public void setId(String in_id) {
        id = in_id;
    }

    public void setCustomer_name(String in_name) {
        customer_name = in_name;
    }

    public void setProduct_count(int ct) {
        product_count = ct;
    }

    public void setTotal_price(int price) {

        total_price = price;
    }

    public void setOrder_num(String o_num) {

        order_num = o_num;
    }

    public void setMisc_info(String info) {
        misc_info = info;
    }


    @Override
    public String toString() {
        return customer_name + " [订单号: " + order_num + "] [" + total_price + " | " + product_count + "] [" + misc_info + "]";
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TicketInfo info = (TicketInfo) o;

        if( (id.equals(info.getId())) && (order_num.equals(info.getOrder_num()))) {
            return true;
        }

        return false;
    }


    @Override
    public int hashCode() {
        return order_num.hashCode();
    }



}
