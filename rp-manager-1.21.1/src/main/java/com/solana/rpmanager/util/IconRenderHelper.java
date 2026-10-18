package com.solana.rpmanager.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class IconRenderHelper {
    private static boolean initialized = false;
    private static Method directDrawMethod = null;      // 9 params (MC 1.20.1 - 1.21.1)
    private static Method pipelineDrawMethod = null;    // 10 params (MC 1.21.2+)
    private static Object pipelineInstance = null;

    private IconRenderHelper() {}

    private static synchronized void init(DrawContext context, Identifier sampleId) {
        if (initialized) return;
        initialized = true;

        try {
            Class<?> ctxClass = context.getClass();

            // 1. Try MC 1.20.1 - 1.21.1 direct method (9 params):
            // (Identifier, int, int, float, float, int, int, int, int)
            for (Method m : ctxClass.getMethods()) {
                Class<?>[] pts = m.getParameterTypes();
                if (pts.length == 9
                        && pts[0].isAssignableFrom(sampleId.getClass())
                        && pts[1] == int.class && pts[2] == int.class
                        && pts[3] == float.class && pts[4] == float.class
                        && pts[5] == int.class && pts[6] == int.class
                        && pts[7] == int.class && pts[8] == int.class) {
                    m.setAccessible(true);
                    directDrawMethod = m;
                    return;
                }
            }

            // 2. Try MC 1.21.2+ / 1.21.11 pipeline method (10 params):
            // (RenderPipeline, Identifier, int, int, float, float, int, int, int, int)
            Object pipeline = null;
            Class<?> rpClass = null;
            try {
                rpClass = Class.forName("net.minecraft.class_10799");
            } catch (ClassNotFoundException e) {
                try {
                    rpClass = Class.forName("com.mojang.blaze3d.pipeline.RenderPipelines");
                } catch (ClassNotFoundException e2) {
                    try {
                        rpClass = Class.forName("net.minecraft.client.gl.RenderPipelines");
                    } catch (ClassNotFoundException ignored) {}
                }
            }

            if (rpClass != null) {
                try {
                    Field f = rpClass.getDeclaredField("field_56883");
                    f.setAccessible(true);
                    pipeline = f.get(null);
                } catch (Throwable ignored) {}

                if (pipeline == null) {
                    for (Field f : rpClass.getDeclaredFields()) {
                        if (f.getName().equalsIgnoreCase("GUI_TEXTURED") || f.getName().equals("field_56883")) {
                            f.setAccessible(true);
                            pipeline = f.get(null);
                            break;
                        }
                    }
                }
            }

            if (pipeline != null) {
                for (Method m : ctxClass.getMethods()) {
                    Class<?>[] pts = m.getParameterTypes();
                    if (pts.length == 10
                            && pts[0].isAssignableFrom(pipeline.getClass())
                            && pts[1].isAssignableFrom(sampleId.getClass())
                            && pts[2] == int.class && pts[3] == int.class
                            && pts[4] == float.class && pts[5] == float.class
                            && pts[6] == int.class && pts[7] == int.class
                            && pts[8] == int.class && pts[9] == int.class) {
                        m.setAccessible(true);
                        pipelineDrawMethod = m;
                        pipelineInstance = pipeline;
                        return;
                    }
                }
            }
        } catch (Throwable t) {
            // Silently ignore to guarantee rendering stability
        }
    }

    public static void drawPackIcon(DrawContext context, Identifier iconId, int x, int y, int width, int height) {
        if (context == null || iconId == null) return;
        try {
            if (!initialized) {
                init(context, iconId);
            }

            if (directDrawMethod != null) {
                directDrawMethod.invoke(context, iconId, x, y, 0.0f, 0.0f, width, height, width, height);
            } else if (pipelineDrawMethod != null && pipelineInstance != null) {
                pipelineDrawMethod.invoke(context, pipelineInstance, iconId, x, y, 0.0f, 0.0f, width, height, width, height);
            }
        } catch (Throwable ignored) {
            // Never crash UI rendering if texture draw fails
        }
    }
}
