package com.facturacion.infrastructure.persistence.mongodb;

import com.facturacion.application.ports.output.ImagenContenido;
import com.facturacion.application.ports.output.ImagenStoragePort;
import com.facturacion.application.ports.output.InfraestructuraNoDisponibleException;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Component
public class GridFSImagenStorageAdapter implements ImagenStoragePort {

    private final GridFsTemplate gridFsTemplate;

    public GridFSImagenStorageAdapter(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    @Override
    public String guardarImagen(byte[] contenido, String nombreArchivo, String contentType) {
        try (InputStream stream = new ByteArrayInputStream(contenido)) {
            ObjectId fileId = gridFsTemplate.store(stream, nombreArchivo, contentType);
            return fileId.toHexString();
        } catch (Exception exc) {
            throw new InfraestructuraNoDisponibleException("MongoDB-GridFS", "Error al guardar imagen en GridFS", exc);
        }
    }

    @Override
    public Optional<ImagenContenido> obtenerImagen(String imagenId) {
        try {
            if (imagenId == null || !ObjectId.isValid(imagenId)) {
                return Optional.empty();
            }
            GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(new ObjectId(imagenId))));
            if (file == null) {
                return Optional.empty();
            }
            GridFsResource resource = gridFsTemplate.getResource(file);
            byte[] bytes = resource.getInputStream().readAllBytes();
            String contentType = file.getMetadata() != null && file.getMetadata().containsKey("_contentType")
                    ? file.getMetadata().getString("_contentType")
                    : "image/jpeg";

            return Optional.of(new ImagenContenido(bytes, file.getFilename(), contentType));
        } catch (IOException exc) {
            throw new InfraestructuraNoDisponibleException("MongoDB-GridFS", "Error al leer imagen de GridFS", exc);
        } catch (Exception exc) {
            throw new InfraestructuraNoDisponibleException("MongoDB-GridFS", "Fallo general en GridFS", exc);
        }
    }

    @Override
    public void eliminarImagen(String imagenId) {
        try {
            if (imagenId != null && ObjectId.isValid(imagenId)) {
                gridFsTemplate.delete(new Query(Criteria.where("_id").is(new ObjectId(imagenId))));
            }
        } catch (Exception exc) {
            throw new InfraestructuraNoDisponibleException("MongoDB-GridFS", "Error al eliminar imagen de GridFS", exc);
        }
    }
}
