package de.cristelknight.cristellib.neoforge.extraapiutil;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.api.CristelLibAPI;
import de.cristelknight.cristellib.api.CristelPlugin;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.util.List;


public class APIFinder {


    public static List<Pair<List<String>, CristelLibAPI>> scanForAPIs() {
        List<Pair<List<String>, CristelLibAPI>> instances = Lists.newArrayList();
        for (ModFileScanData data : ModList.get().getAllScanData()) {
            List<ModFileScanData.AnnotationData> ebsTargets = data.getAnnotations().stream().
                    filter(annotationData -> Type.getType(CristelPlugin.class).equals(annotationData.annotationType())).
                    toList();

            List<String> modIds = data.getIModInfoData().stream()
                    .flatMap(info -> info.getMods().stream())
                    .map(IModInfo::getModId)
                    .toList();

            for (ModFileScanData.AnnotationData ad : ebsTargets) {
                Class<CristelLibAPI> clazz;

                try {
                    clazz = (Class<CristelLibAPI>) Class.forName(ad.memberName());
                } catch (ClassNotFoundException e) {
                    CristelLib.LOGGER.error("Failed to load api class: " + ad.clazz() + " for @CristelPlugin annotation", e);
                    continue;
                }
                try {
                    instances.add(new Pair<>(modIds, clazz.getDeclaredConstructor().newInstance()));
                } catch (Throwable throwable) {
                    CristelLib.LOGGER.error("Failed to load api: " + ad.memberName(), throwable);
                }
            }
        }
        return instances;
    }
}
