package cz.Empatix.Render;

class RoomPath {
    // corners of room
    private int yMin;
    private int yMax;
    private int xMin;
    private int xMax;

    // corners of room
    private int realYMin;
    private int realYMax;
    private int realXMin;
    private int realXMax;

    void setCorners(int xMin,int xMax,int yMin, int yMax){
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.yMin = yMin;
    }
    void setRealCorners(int xMin,int xMax,int yMin, int yMax){
        this.realXMin = xMin;
        this.realXMax = xMax;
        this.realYMax = yMax;
        this.realYMin = yMin;
    }
    int getyMin() { return yMin; }

    int getxMax() { return xMax; }

    int getxMin() { return xMin; }

    int getyMax() { return yMax; }

    public int getRealXMax() {
        return realXMax;
    }

    public int getRealXMin() {
        return realXMin;
    }

    public int getRealYMax() {
        return realYMax;
    }

    public int getRealYMin() {
        return realYMin;
    }
}

