package model.entity;

public class Photon extends Entity{
    @Override
    public void initBasePolygon() {
        basePolygon.addPoint(1, 1);
        basePolygon.addPoint(1, -1);
        basePolygon.addPoint(-1, 1);
        basePolygon.addPoint(-1, -1);
    }
}
