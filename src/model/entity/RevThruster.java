package model.entity;

public class RevThruster extends Entity{
    @Override
    public void initBasePolygon() {
        basePolygon.addPoint(-2, 12);
        basePolygon.addPoint(-4, 14);
        basePolygon.addPoint(-2, 20);
        basePolygon.addPoint(0, 14);
        basePolygon.addPoint(2, 12);
        basePolygon.addPoint(4, 14);
        basePolygon.addPoint(2, 20);
        basePolygon.addPoint(0, 14);

    }
}
