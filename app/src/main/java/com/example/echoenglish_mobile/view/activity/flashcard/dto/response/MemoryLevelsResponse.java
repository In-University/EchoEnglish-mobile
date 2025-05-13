package com.example.echoenglish_mobile.view.activity.flashcard.dto.response;

public class MemoryLevelsResponse {
    private long level0;
    private long level1;
    private long level2;
    private long level3;
    private long level4;
    private long mastered;

    public MemoryLevelsResponse() {
    }

    public MemoryLevelsResponse(long level0, long level1, long level2, long level3, long level4, long mastered) {
        this.level0 = level0;
        this.level1 = level1;
        this.level2 = level2;
        this.level3 = level3;
        this.level4 = level4;
        this.mastered = mastered;
    }

    public long getLevel0() {
        return level0;
    }

    public void setLevel0(long level0) {
        this.level0 = level0;
    }

    public long getLevel1() {
        return level1;
    }

    public void setLevel1(long level1) {
        this.level1 = level1;
    }

    public long getLevel2() {
        return level2;
    }

    public void setLevel2(long level2) {
        this.level2 = level2;
    }

    public long getLevel3() {
        return level3;
    }

    public void setLevel3(long level3) {
        this.level3 = level3;
    }

    public long getLevel4() {
        return level4;
    }

    public void setLevel4(long level4) {
        this.level4 = level4;
    }

    public long getMastered() {
        return mastered;
    }

    public void setMastered(long mastered) {
        this.mastered = mastered;
    }
}