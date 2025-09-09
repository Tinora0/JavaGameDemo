module com.example.demo2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires com.almasb.fxgl.all;
    opens assets.textures;
    opens assets.sounds;
    opens com.example.demo2;
    exports com.example.demo2;

}
