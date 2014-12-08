package com.adriansoghoian.wearexperiments;


import java.util.ArrayList;

/**
 * Created by  on 11/30/14.
 */
public class dtw {

    public static float dtw(float[] sequence1, float[] sequence2) {
        float temp = (float)0.0;
        int cost_x = sequence1.length + 1;
        int cost_y = sequence2.length + 1;
        float[][] cost = new float[cost_x][cost_y];

        for(int i = 1; i < cost_x; i++) {
            cost[i][0] = Float.POSITIVE_INFINITY;
        }
        for(int i = 1; i < cost_y; i++) {
            cost[0][i] = Float.POSITIVE_INFINITY;
        }
        cost[0][0] = 0;

        for(int i = 1; i < cost_x; i++) {
            for(int j = 1; j < cost_y; j++) {
                temp = Math.abs(sequence1[i - 1] - sequence2[j - 1]);
                cost[i][j] = temp + min(cost[i - 1][j], cost[i][j - 1], cost[i -1][j - 1]);
            }
        }
        return cost[cost_x - 1][cost_y - 1];
    }

    public static float min(float a, float b, float c) {
        if (a <= b && a <= c) return a;
        if (b <= a && b <= c) return b;
        return c;
    }

    public static float compareMeasurements(ArrayList<float[]> measurement1, ArrayList<float[]> measurement2) {
        float[] seq1X = new float[measurement1.size()];
        float[] seq1Y = new float[measurement1.size()];
        float[] seq1Z = new float[measurement1.size()];

        float[] seq2X = new float[measurement2.size()];
        float[] seq2Y = new float[measurement2.size()];
        float[] seq2Z = new float[measurement2.size()];

        float scoreX = (float)0.0;
        float scoreY = (float)0.0;
        float scoreZ = (float)0.0;

        for(int i = 0; i < measurement1.size(); i++) {
            seq1X[i] = (measurement1.get(i))[0];
            seq1Y[i] = (measurement1.get(i))[1];
            seq1Z[i] = (measurement1.get(i))[2];
        }
        for(int i = 0; i < measurement2.size(); i++) {
            seq2X[i] = (measurement2.get(i))[0];
            seq2Y[i] = (measurement2.get(i))[1];
            seq2Z[i] = (measurement2.get(i))[2];
        }
        scoreX = dtw(seq1X, seq2X);
        scoreY = dtw(seq1Y, seq2Y);
        scoreZ = dtw(seq1Z, seq2Z);

        return (scoreX + scoreY + scoreZ) / (float)3.0;
    }
}
