package model.entity;

public class FwdThruster extends Entity{
    @Override
    public void initBasePolygon() {
        basePolygon.addPoint(0, 12);
        basePolygon.addPoint(-3, 16);
        basePolygon.addPoint(0, 26);
        basePolygon.addPoint(3, 16);

    }

}
