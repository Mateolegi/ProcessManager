module com.mateolegi {
    requires javafx.controls;
    requires javafx.fxml;
    requires slf4j.api;
    requires org.apache.commons.io;

    opens com.mateolegi.despliegues_audiencias to javafx.fxml;
    exports com.mateolegi.despliegues_audiencias;
}