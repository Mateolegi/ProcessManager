package com.mateolegi.despliegues_audiencias.process.impl;

import com.mateolegi.despliegues.process.AsyncProcess;
import com.mateolegi.despliegues_audiencias.util.AntManager;
import com.mateolegi.despliegues_audiencias.util.Configuration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static com.mateolegi.despliegues_audiencias.util.ProcessFactory.setValue;

/**
 * Realiza el proceso de generación de los desplegables de Audiencias.
 */
public class AudienciasGeneration implements AsyncProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(AudienciasGeneration.class);
    private static final Configuration CONFIGURATION = new Configuration();

    private final File outputDirectory;

    public AudienciasGeneration() {
        outputDirectory = new File(CONFIGURATION.getOutputDirectory());
    }

    /**
     * Valida que no exista el directorio con los desplegables de Audiencias,
     * si existe entonces los elimina y los vuelve a crear.
     * @return si falla alguna de esas operaciones entonces retorna {@code false},
     * en otro caso retorna {@code true}.
     */
    @Override
    public boolean prepare() {
        var audienciasOutput = new File(outputDirectory, "audiencias");
        if (audienciasOutput.exists()) {
            LOGGER.debug("Se elimina el directorio de jar antiguo");
            setValue("Eliminando directorio de jar antiguo...");
            try {
                FileUtils.deleteDirectory(audienciasOutput);
            } catch (IOException e) {
                LOGGER.error("Error eliminando el directorio del jar de Audiencias", e);
                return false;
            }
        }
        LOGGER.debug("Creando directorio para el jar de Audiencias");
        return audienciasOutput.mkdirs();
    }

    /**
     * Crea un futuro donde se ejecuta el comando {@code ant}
     * desde una consola de {@code shell unix}.
     * Nota: debe haber un archivo {@code build.xml} con las indicaciones
     * para generar el desplegable en la ruta de salida
     * del despliegue especificada en el archivo de configuración
     * con la propiedad {@code output.directory}.
     * @return futuro que retorna un entero donde 0 significa que el
     * proceso fue exitoso, y {@code AUDIENCIAS_JAR_GENERATION}(1)
     * si ocurrió algún error.
     */
    @Override
    public CompletableFuture<Integer> start() {
        LOGGER.debug("Se procede a la generación del Jar de Audiencias.");
        setValue("Generando jar de Audiencias...");
        return CompletableFuture.supplyAsync(() -> {
            new AntManager().build();
            return 0;
        });
    }

    /**
     * Valida que el jar se haya generacdo y se haya creado
     * la carpeta con las librerías de terceros.
     * @return {@code true} si las validadiones fueron correctas.
     */
    @Override
    public boolean validate() {
        LOGGER.debug("Validando la creación del Jar y la exportación de las librerías.");
        setValue("Validando la creación del jar y la exportación de las librerías...");
        var audienciasOutput = new File(outputDirectory, "audiencias");
        if (audienciasOutput.exists() && audienciasOutput.isDirectory()) {
            var audienciasJar = new File(audienciasOutput, "audiencias.jar");
            var audienciasLib = new File(audienciasOutput, "audiencias_lib");
            LOGGER.debug("Se generó correctamente el Jar de Audiencias.");
            return audienciasJar.exists() && audienciasLib.exists();
        }
        return false;
    }
}
