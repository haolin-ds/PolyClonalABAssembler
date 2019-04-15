package com.uwaterloo;

import org.junit.Test;
import org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MutationsPattern {
    List<Integer> posList;
    String AAs;
    int freq;
    int score;
    Set<Long> intensitySet;

    public MutationsPattern(List<Integer> posList, String AAs, int freq, int score) {
        this.posList = posList;
        this.AAs = AAs;
        this.freq = freq;
        this.score = score;
        intensitySet = null;
    }

    public MutationsPattern(List<Integer> posList, String AAs, int freq, int score, Set<Long> intensitySet) {
        this.posList = posList;
        this.AAs = AAs;
        this.freq = freq;
        this.score = score;
        this.intensitySet = intensitySet;
    }

    public MutationsPattern(List<Integer> posList, String AAs) {
        this.posList = posList;
        this.AAs = AAs;
        this.freq = 0;
        this.score = 0;
    }

    public List<Integer> getPosList() {
        return posList;
    }

    public String getAAs() {
        return AAs;
    }

    public int getFreq() {
        return freq;
    }

    public int getScore() {
        return score;
    }

    public void setPosList(List<Integer> posList) {
        this.posList = posList;
    }

    public void setAAs(String AAs) {
        this.AAs = AAs;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Set<Long> getIntensitySet() {
        return intensitySet;
    }

    public long getIntensity() {
        long sum = 0;
        if (intensitySet == null) {
            return 0;
        }
        for (long intensity : intensitySet) {
            sum += intensity;
        }
        return sum;
    }

    public void setIntensitySet(Set<Long> intensitySet) {
        this.intensitySet = intensitySet;
    }

    @Override
    public String toString() {
        return AAs + " at " +  posList.toString() + " freq: " + freq +
                " score: " + score + " intensity: " + getIntensity();
    }

    @Override
    public boolean equals(Object o) {
        if (!this.getAAs().equals(((MutationsPattern) o).getAAs())) {
            return false;
        }
        if (this.getPosList().equals(((MutationsPattern) o).getPosList())) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 7;
        result = 31 * result + posList.toString().hashCode();
        result = 31 * result + AAs.hashCode();
        return result;
    }

    public static void main(String[] args) {
        List<Integer> posList1 = new ArrayList<>();
        posList1.add(18);
        posList1.add(27);

        List<Integer> posList2 = new ArrayList<>();
        posList2.add(18);
        posList2.add(27);

        MutationsPattern pattern1 = new MutationsPattern(posList1,"DG",3, 26);
        MutationsPattern pattern2 = new MutationsPattern(posList2, "DG", 2, 28);

        HashMap<MutationsPattern, Integer> map = new HashMap<>();
        map.put(pattern1, 1);
        if (map.containsKey(pattern2)) {
            System.out.println("same");
        }

        boolean isEqual = (pattern1.equals(pattern2));
        System.out.println(isEqual);

    }

}
