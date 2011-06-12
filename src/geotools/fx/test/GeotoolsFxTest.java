package geotools.fx.test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import java.io.File;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;

public class GeotoolsFxTest extends Application {
    Group root = new Group();
    Group map1 = new Group();
    Group map = new Group();
    Group texts = new Group();
    Scene scene;

    private double dragBaseX, dragBaseY;
    private double dragBase2X, dragBase2Y;

    public static void main(String[] args) {
        Application.launch(GeotoolsFxTest.class, args);
    }
    
    private void zoom(double d) {
        map.scaleXProperty().set(map.scaleXProperty().get() * d);
        map.scaleYProperty().set(map.scaleYProperty().get() * d);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Geotools FX Test");
        scene = new Scene(root, 300, 250, Color.LIGHTBLUE);
        Color[] colors = new Color[] { Color.YELLOW, Color.RED, Color.ORANGE, Color.VIOLET, Color.CHOCOLATE, Color.YELLOW, Color.AZURE };
        int currentColor=0;
        
        File file = new File("110m_cultural\\110m_admin_0_countries.shp");
        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();
        SimpleFeatureCollection c = featureSource.getFeatures();
        SimpleFeatureIterator featuresIterator = c.features();
        Coordinate[] coords;
        Geometry polygon;
        Point centroid;
        Bounds bounds;
        while (featuresIterator.hasNext()) {
            SimpleFeature o = featuresIterator.next();
            String name = (String) o.getAttribute("NAME");
            Object geometry = o.getDefaultGeometry();

            if (geometry instanceof MultiPolygon) {
                MultiPolygon multiPolygon = (MultiPolygon) geometry;

                centroid = multiPolygon.getCentroid();
                final Text text = new Text(name);
                bounds = text.getBoundsInLocal();
                text.getTransforms().add(new Translate(centroid.getX(), centroid.getY()));
                text.getTransforms().add(new Scale(0.1,-0.1));
                text.getTransforms().add(new Translate(-bounds.getWidth()/2., bounds.getHeight()/2.));
                texts.getChildren().add(text);
                
                for (int geometryI=0;geometryI<multiPolygon.getNumGeometries();geometryI++) {
                    polygon = multiPolygon.getGeometryN(geometryI);
                    
                    coords = polygon.getCoordinates();
                    Path path = new Path();
                    path.setStrokeWidth(0.05);
                    currentColor = (currentColor+1)%colors.length;
                    path.setFill(colors[currentColor]);
                    path.getElements().add(new MoveTo(coords[0].x, coords[0].y));
                    for (int i=1;i<coords.length;i++) {
                        path.getElements().add(new LineTo(coords[i].x, coords[i].y));
                    }
                    path.getElements().add(new LineTo(coords[0].x, coords[0].y));
                    map1.getChildren().add(path);
                }
            }
        }
        
        map.translateXProperty().set(600);
        map.translateYProperty().set(300);
        map.scaleXProperty().set(3);
        map.scaleYProperty().set(-3);
        map.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                scene.setCursor(Cursor.MOVE);
                dragBaseX = map.translateXProperty().get();
                dragBaseY = map.translateYProperty().get();
                dragBase2X = event.getSceneX();
                dragBase2Y = event.getSceneY();
            }
        });
        map.setOnMouseDragged(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                map.setTranslateX(dragBaseX + (event.getSceneX()-dragBase2X));
                map.setTranslateY(dragBaseY + (event.getSceneY()-dragBase2Y));
            }
        });
        map.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                scene.setCursor(Cursor.DEFAULT);
            }
        });

        map.getChildren().add(map1);
        map.getChildren().add(texts);
        root.getChildren().add(map);
        VBox vbox = new VBox();
        final Button plus = new Button("+");
        plus.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                zoom(1.4);
            }
        });
        vbox.getChildren().add(plus);
        final Button minus = new Button("-");
        minus.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                zoom(1./1.4);
            }
        });
        vbox.getChildren().add(minus);
        root.getChildren().add(vbox);
        
        primaryStage.setScene(scene);
        primaryStage.setVisible(true);
    }
}
