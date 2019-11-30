package com.ba.ecgreader;

import java.util.LinkedList;

public class Sync {

    public static class Syncronization {
        // Create a list shared by producer and consumer
        // Size of list is 2.
        LinkedList<String> list = new LinkedList<>();
        int capacity = 25000;

        // Function called by producer thread
        public void produce(String data) throws InterruptedException {
            synchronized (this) {

                while (list.size() == capacity)
                    wait();

                // to insert the jobs in the list
                list.add(data);

                // notifies the consumer thread that
                // now it can start consuming
                notify();

                // makes the working of program easier
                // to  understand
                //Thread.sleep(5);
            }
        }

        // Function called by consumer thread
        public String consume() throws InterruptedException {
            String val="";
            synchronized (this) {
                // consumer thread waits while list
                // is empty
                while (list.size() == 0)
                    wait();

                //to retrive the ifrst job in the list
                val = list.removeFirst();

                // Wake up producer thread
                notify();

                // and sleep
                //Thread.sleep(5);
            }
        return val;
        }
    }
    public static class Syncronization2 {
        // Create a list shared by producer and consumer
        // Size of list is 2.
        public LinkedList<String> list = new LinkedList<>();
        int capacity = 25000;

        // Function called by producer thread
        public void produce(String data) throws InterruptedException {
            synchronized (this) {

                while (list.size() == capacity)
                    wait();

                // to insert the jobs in the list
                list.add(data);

                // notifies the consumer thread that
                // now it can start consuming
                notify();

                // makes the working of program easier
                // to  understand
                //Thread.sleep(5);
            }
        }

        // Function called by consumer thread
        public String consume() throws InterruptedException {
            String val="";
            synchronized (this) {
                // consumer thread waits while list
                // is empty
                while (list.size() == 0)
                    //wait();
                    return null;

                //to retrive the ifrst job in the list
                val = list.removeFirst();

                // Wake up producer thread
                notify();

                // and sleep
                //Thread.sleep(5);
            }
            return val;
        }
    }
}