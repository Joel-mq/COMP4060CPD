package com.example.gpsnotifier;

public class BusRoute {
    BusStop[] busStops;
    int i = 0;

    public BusRoute(int stopsLength) {
        busStops = new BusStop[stopsLength];
    }

    public void addStop(double lat, double lon, String nam) {
        if (i < busStops.length) {
            busStops[i] = new BusStop(lat, lon, nam);
            i++;
        } else {
            System.out.println("too many stops, redefine route length better >:(");
        }
    }

    public String[] returnStringOfBusStops() {
        String[] tempString = new String[i];
            for (int j = 0; j < i; j++) {
                tempString[j] = busStops[j].name;
            }
        return tempString;
    }
}
