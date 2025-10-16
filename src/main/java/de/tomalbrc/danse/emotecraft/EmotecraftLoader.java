package de.tomalbrc.danse.emotecraft;

import com.google.gson.JsonParseException;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.file.bbmodel.BbModel;
import de.tomalbrc.bil.file.loader.AjBlueprintLoader;
import de.tomalbrc.bil.file.loader.ModelLoader;
import de.tomalbrc.danse.bbmodel.PlayerModelImporter;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

public class EmotecraftLoader extends AjBlueprintLoader {
    List<EmotecraftAnimationFile> animationFiles = new ObjectArrayList<>();

    public void add(EmotecraftAnimationFile file) {
        animationFiles.add(file);
    }

    @Override
    public Model load(InputStream input, String name) throws JsonParseException {
        try (Reader reader = new InputStreamReader(input)) {
            BbModel model = GSON.fromJson(reader, BbModel.class);

            if (name != null && !name.isEmpty()) model.modelIdentifier = name;
            if (model.modelIdentifier == null) model.modelIdentifier = model.name;
            model.modelIdentifier = ModelLoader.normalizedModelId(model.modelIdentifier);

            for (EmotecraftAnimationFile file : animationFiles) {
                EmoteConverter.convertAndAddTo(file, model);
            }

            return new PlayerModelImporter(model).importModel();
        } catch (Throwable throwable) {
            throw new JsonParseException("Failed to parse: " + name, throwable);
        }
    }
}
