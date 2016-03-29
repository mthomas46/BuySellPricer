package com.mjt;

import java.text.DecimalFormat;
import java.util.*;
import java.io.*;

class Main {

    static String addOrderTemplate = "28800538 A b S 44.26 100";
    static String reduceOrderTemplate = "28800744 R b 100";
    static String reduceOrder = "R";
    static String buy = "B";
    static String sell = "S";
    private static final String del=" ";
    private static int curAskSize;
    private static float curAskTotal;
    private static float oldAskTotal;
    private static int curBidSize;
    private static float curBidTotal;
    private static float oldBidTotal;
    private static int timeStamp;
    private static final DecimalFormat formatter =new DecimalFormat("#0.00");


    public static void main(String[] args) throws Exception {
        Comparator<Entry> entryAskComparator = (o1, o2) -> {
            if (o1.price < o2.price)
                return -1;
            else if (o1.price > o2.price)
                return 1;
            else
                return 0;
        };
        Comparator<Entry> entryBidComparator = (o1, o2) -> {
            if (o1.price > o2.price)
                return -1;
            else if (o1.price < o2.price)
                return 1;
            else
                return 0;
        };
        PriorityQueue<Entry> askHeap = new PriorityQueue<>(20, entryAskComparator);
        PriorityQueue<Entry> bidHeap = new PriorityQueue<>(20, entryBidComparator);

        int targetSize = Integer.parseInt(args[0]);
        String filePath = args[1];

        Map<String, AddOrder> orderBook = new HashMap<>();
        AddOrder newAddOrder;
        ReduceOrder newReduceOrder;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int index = 0;
            //READS LINE
            while ((line = br.readLine()) != null ) {

                //PRINT OUT CHANGES
                if (curAskTotal != oldAskTotal)
                    System.out.println(timeStamp + del + buy + del + (curAskTotal == 0.0 ? "NA" : formatter.format(curAskTotal)));
                else if (curBidTotal != oldBidTotal)
                    System.out.println(timeStamp + del + sell + del + (curBidTotal == 0.0 ? "NA" : formatter.format(curBidTotal)));
                oldAskTotal = curAskTotal;
                oldBidTotal = curBidTotal;

                curAskTotal = 0;
                curBidTotal = 0;
                //ADD TO APPROPRIATE HEAP
                //UPDATE SIZES
                try {
                    if (parseOrder(line, index).getClass().equals(AddOrder.class)) {
                        newAddOrder = (AddOrder) parseOrder(line, index);
                        timeStamp = newAddOrder.timestamp;
                        if (newAddOrder.side.equals("B")) {
                            bidHeap.add(new Entry(newAddOrder.id, newAddOrder.price));
                            curBidSize += newAddOrder.numStocks;
                        } else if (newAddOrder.side.equals("S")) {
                            askHeap.add(new Entry(newAddOrder.id, newAddOrder.price));
                            curAskSize += newAddOrder.numStocks;
                        }
                        orderBook.put(newAddOrder.id, newAddOrder);
                    }

                    //REDUCE ORDER
                    else if (parseOrder(line, index).getClass().equals(ReduceOrder.class)) {
                        newReduceOrder = (ReduceOrder) parseOrder(line, index);
                        timeStamp = newReduceOrder.timestamp;

                        if (orderBook.containsKey(newReduceOrder.id)) {
                            orderBook.get(newReduceOrder.id).numStocks -= newReduceOrder.numStocks;

                            //UPDATES CURRENT TARGET TOTALS
                            if (orderBook.get(newReduceOrder.id).side.equals(buy))
                                curBidSize -= newReduceOrder.numStocks;
                            else if (orderBook.get(newReduceOrder.id).side.equals(sell))
                                curAskSize -= newReduceOrder.numStocks;

                            //REMOVES INVALID ORDERS
                            if (orderBook.get(newReduceOrder.id).numStocks <= 0) {
                                Entry entryToBeRemoved = new Entry(newReduceOrder.id, orderBook.get(newReduceOrder.id).price);
                                if (orderBook.get(newReduceOrder.id).side.equals(sell)) {
                                    resolveRemove(askHeap,entryToBeRemoved);
                                } else if (orderBook.get(newReduceOrder.id).side.equals(buy)) {
                                    resolveRemove(bidHeap,entryToBeRemoved);
                                }

                                orderBook.remove(newReduceOrder.id);
                            }
                        }
                    }
                }catch(Exception e){
                    System.out.println(e);
                }
                //UPDATE ASK PRICES
                if (curAskSize >= targetSize) {
                    int askCounter = curAskSize;
                    PriorityQueue<Entry> tempAskHeap = new PriorityQueue<>(askHeap);
                    while (askCounter > (curAskSize - targetSize)) {
                        if (orderBook.containsKey(tempAskHeap.peek().id)) {
                            if ((askCounter - orderBook.get(tempAskHeap.peek().id).numStocks) >= (curAskSize - targetSize)) {
                                AddOrder tempOrder = orderBook.get(tempAskHeap.remove().id);
                                curAskTotal += tempOrder.numStocks * tempOrder.price;
                                askCounter -= orderBook.get(tempOrder.id).numStocks;
                            } else {
                                AddOrder tempOrder = orderBook.get(tempAskHeap.remove().id);
                                curAskTotal += tempOrder.price * (askCounter - (curAskSize - targetSize));
                                askCounter -= (askCounter - (curAskSize - targetSize));
                            }
                        } else {
                            tempAskHeap.remove();
                            askHeap.remove();
                        }
                    }
                }
                //UPDATE BID PRICES
                if (curBidSize >= targetSize) {
                    int bidCounter = curBidSize;
                    PriorityQueue<Entry> tempBidHeap = new PriorityQueue<>(bidHeap);
                    while (bidCounter > (curBidSize - targetSize)) {
                        if (orderBook.containsKey(tempBidHeap.peek().id)) {
                            if ((bidCounter - orderBook.get(tempBidHeap.peek().id).numStocks) >= (curBidSize - targetSize)) {
                                AddOrder tempOrder = orderBook.get(tempBidHeap.remove().id);
                                curBidTotal += tempOrder.numStocks * tempOrder.price;
                                bidCounter -= orderBook.get(tempOrder.id).numStocks;
                            } else {
                                AddOrder tempOrder = orderBook.get(tempBidHeap.remove().id);
                                curBidTotal += tempOrder.price * (bidCounter - (curBidSize - targetSize));
                                bidCounter -= (bidCounter - (curBidSize - targetSize));
                            }
                        } else {
                            tempBidHeap.remove();
                            bidHeap.remove();
                        }
                    }
                }
                index++;
            }
            System.out.println("ORDER BOOK:\t" + orderBook);
        } catch (IOException e) {
            System.out.println("Exception:\t"+e);
        }
        System.out.println("DONE");
    }

    static boolean validateOrder(String line) {
        String regexValidator = "\\d+\\sR\\s\\w+\\s+\\d+|\\d+\\sA\\s\\w+\\s[SB]\\s\\d+\\.\\d{2,}\\s\\d+";
        return line.matches(regexValidator);
    }

    static Order parseOrder(String line, int lineNumber) throws Exception {
        if (validateOrder(line)) {
            String delim = "[ ]+";
            String[] params = line.split(delim);
            String addOrder = "A";
            if (params[1].equals(addOrder))
                return new AddOrder(Integer.parseInt(params[0]), params[1], params[2], params[3], Float.parseFloat(params[4]), Integer.parseInt(params[5]), lineNumber);
            else
                return new ReduceOrder(Integer.parseInt(params[0]), params[1], params[2], Integer.parseInt(params[3]), lineNumber);
        } else
            throw new Exception("Line: "+lineNumber+"\t->\t"+line+"\tIs malformed and can not be parsed.");
    }

    private static void resolveRemove(PriorityQueue<Entry> heap, Entry entryToBeRemoved){
        Entry tempEntry;
        PriorityQueue<Entry> tempQueue = new PriorityQueue<>(20, heap.comparator());
        while (!heap.isEmpty()) {
            tempEntry = heap.remove();
            if (!tempEntry.equals(entryToBeRemoved))
                tempQueue.add(tempEntry);
        }
        heap.addAll(tempQueue);
    }

}

class Entry {
    final String id;
    final float price;

    public Entry( String id,  float price) {
        this.id = id;
        this.price = price;
    }

    public String getId() {
        return this.id;
    }

    public float getPrice() {
        return this.price;
    }

    @Override
    public String toString() {
        return ("id: " + id + "\tprice: " + price);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass()!=Entry.class)
            return false;
        Entry temp = (Entry) obj;
        return (this.id.equals(temp.id) && this.price == temp.price);
    }
}

class Order {
    final String del=" ";
    final int timestamp;
    final String id;
    int numStocks;
    private final int lineNumber;

    Order( int timestamp,  String id,  int numStocks,  int lineNumber) {
        this.timestamp = timestamp;
        this.id = id;
        this.numStocks = numStocks;
        this.lineNumber = lineNumber;
    }
}

class AddOrder extends Order {
    private final String A;
    final String side;
    final float price;

    public AddOrder( int timestamp,  String A,  String id,  String side,  float price,  int numStocks,  int lineNumber) {
        super(timestamp, id, numStocks, lineNumber);
        this.price = price;
        this.A = A;
        this.side = side;

    }

    @Override
    public String toString() {
        return (timestamp + del + A + del + id + del + side + del + price + del + numStocks);
    }
}

class ReduceOrder extends Order {
    private final String R;

    public ReduceOrder( int timestamp,  String R,  String id,  int numStocks,  int lineNumber) {
        super(timestamp, id, numStocks, lineNumber);
        this.R = R;
    }

    @Override
    public String toString() {
        return (timestamp + del + R + del + id + del + numStocks);
    }
}

