package model.entity;

public class Photon extends Entity{
    @Override
    public void initBasePolygon() {
        basePolygon.addPoint(1, 1);
        basePolygon.addPoint(1, -1);
        basePolygon.addPoint(-1, 1);
        basePolygon.addPoint(-1, -1);
    }

    @Override
    public String toString() {
        return "Photon{" +
                "active=" + active +
                ", angle=" + angle +
                ", deltaAngle=" + deltaAngle +
                ", x=" + x +
                ", y=" + y +
                ", deltaX=" + deltaX +
                ", deltaY=" + deltaY +
                '}';
    }
}
