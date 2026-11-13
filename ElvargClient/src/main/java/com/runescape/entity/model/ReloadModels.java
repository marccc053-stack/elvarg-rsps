package com.runescape.entity.model;

import com.runescape.sign.SignLink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

/**
 * Loads custom models that are not packed into the cache.
 *
 * Put the model files of a model pack in Cache/reload_models/ and name each one after the
 * model id it must get: 65000.dat (or 65000.gz when it is gzipped). Ids up to 79999 work.
 * Which item uses which model is set in tools/reload-world/content.py (MODELS).
 */
public final class ReloadModels {

    private static final String DIRECTORY = "reload_models";

    public static void load() {
        File[] files = new File(SignLink.findcachedir() + DIRECTORY).listFiles();
        if (files == null) {
            return;
        }
        int loaded = 0;
        for (File file : files) {
            String name = file.getName();
            int dot = name.lastIndexOf('.');
            if (dot <= 0) {
                continue;
            }
            try {
                int id = Integer.parseInt(name.substring(0, dot));
                byte[] data = Files.readAllBytes(file.toPath());
                if (name.endsWith(".gz")) {
                    data = gunzip(data);
                }
                Model.method460(data, id);
                loaded++;
            } catch (Exception e) {
                System.out.println("Could not load custom model " + name + ": " + e);
            }
        }
        System.out.println("Loaded: " + loaded + " custom models");
    }

    private static byte[] gunzip(byte[] data) throws Exception {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(data));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }

    private ReloadModels() {
    }
}
