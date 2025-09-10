module com.example.demo2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;
    opens assets.textures;
    opens assets.sounds;
    opens assets.levels;
    opens com.example.demo2;
    exports com.example.demo2;

}
