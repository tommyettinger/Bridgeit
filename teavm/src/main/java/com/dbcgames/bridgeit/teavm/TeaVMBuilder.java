package com.dbcgames.bridgeit.teavm;

import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFileHandle;
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompiler;
import com.github.xpenatan.gdx.teavm.backends.web.config.backend.WebBackend;
import java.io.File;
import org.teavm.tooling.TeaVMSourceFilePolicy;
import org.teavm.tooling.sources.DirectorySourceFileProvider;
import org.teavm.vm.TeaVMOptimizationLevel;

/** Builds the TeaVM/HTML application. */
public class TeaVMBuilder {
    public static void main(String[] args) {
        // Typically set by the Gradle task, but can also be set here or with the command-line arg "debug"
        boolean debug = false;
        // Typically set by the Gradle task, but can also be set here or with the command-line arg "run"
        boolean startJetty = false;
        for (String arg : args) {
            if ("debug".equals(arg)) debug = true;
            else if ("run".equals(arg)) startJetty = true;
        }
        new TeaCompiler(
            new WebBackend()
                .setHtmlWidth(800) /* Change this to fit your game's requirements. */
                .setHtmlHeight(600) /* Change this to fit your game's requirements. */
                .setHtmlTitle("Bridgeit")
                .setWebAssembly(true) /* Uncomment this line to use WASM output instead of JavaScript output. */
                .setStartJettyAfterBuild(startJetty)
                .setJettyPort(8080)
        )
            .addAssets(new AssetFileHandle("../assets"))

            .setOptimizationLevel(debug ? TeaVMOptimizationLevel.SIMPLE : TeaVMOptimizationLevel.ADVANCED)
            .setMainClass(TeaVMLauncher.class.getName())
            .setObfuscated(!debug)
            .setDebugInformationGenerated(debug)
            .setSourceMapsFileGenerated(debug)
            .setSourceFilePolicy(TeaVMSourceFilePolicy.COPY)
            .addSourceFileProvider(new DirectorySourceFileProvider(new File("../core/src/main/java/")))
            // You can also register any classes or packages that require reflection here:
            .addReflectionClass("com.dbcgames.bridgeit")
            .addReflectionClass("java.util.Collection")
            .addReflectionClass("java.util.List")
            .addReflectionClass("java.util.ArrayList")
            .addReflectionClass("java.util.Map")
            .addReflectionClass("java.util.HashMap")
            .addReflectionClass("java.util.BitSet")
            .addReflectionClass("java.lang.String")
            .addReflectionClass("java.lang.Boolean")
            .addReflectionClass("java.lang.Byte")
            .addReflectionClass("java.lang.Short")
            .addReflectionClass("java.lang.Character")
            .addReflectionClass("java.lang.Integer")
            .addReflectionClass("java.lang.Float")
            .addReflectionClass("java.lang.Double")
            .addReflectionClass("java.lang.CharSequence")
            .addReflectionClass("java.lang.Enum")
            .addReflectionClass("java.lang.Object")
            .addReflectionClass("com.artemis.Aspect")
            .addReflectionClass("com.artemis.Aspect.Builder")
            .addReflectionClass("com.artemis.AspectSubscriptionManager")
            .addReflectionClass("com.artemis.BaseEntitySystem")
            .addReflectionClass("com.artemis.BaseSystem")
            .addReflectionClass("com.artemis.components")
            .addReflectionClass("com.artemis.PackedComponent")
            .addReflectionClass("com.artemis.PooledComponent")
            .addReflectionClass("com.artemis.annotations.AspectDescriptor")
            .addReflectionClass("com.artemis.annotations.DelayedComponentRemoval")
            .addReflectionClass("com.artemis.annotations.LinkPolicy")
            .addReflectionClass("com.artemis.annotations.Wire")
            .addReflectionClass("com.artemis.ComponentType")
            .addReflectionClass("com.artemis.ComponentMapper")
            .addReflectionClass("com.artemis.Component")
            .addReflectionClass("com.artemis.EntityManager")
            .addReflectionClass("com.artemis.EntitySystem")
            .addReflectionClass("com.artemis.Entity")
            .addReflectionClass("com.artemis.Manager")
            .addReflectionClass("com.artemis.systems")
            .addReflectionClass("com.artemis.managers")
            .addReflectionClass("com.artemis.World")
            .addReflectionClass("com.artemis.io")
            .addReflectionClass("com.artemis.Archetype")
            .addReflectionClass("com.artemis.ArtemisPlugin")
            .addReflectionClass("com.artemis.link.EntityLinkManager")
            .addReflectionClass("com.artemis.injection.UseInjectionCache")
            .addReflectionClass("com.artemis.injection.ArtemisFieldResolver")
            .addReflectionClass("com.artemis.injection.WiredFieldResolver")
            .addReflectionClass("com.artemis.injection.AspectFieldResolver")
            .addReflectionClass("com.artemis.injection.PojoFieldResolver")
            .addReflectionClass("com.artemis.utils.BitVector")

            .build(new File("build/dist"));
    }
}
