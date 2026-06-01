package dev.rm20.anglersalmanac.Utils;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.builder.BuilderField;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.codec.validation.Validators;
import dev.rm20.anglersalmanac.Utils.Annotations.CodecAnnotations;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

public class AutoCodecBuilder {

    private static final Map<Class<?>, Codec<?>> REGISTRY = new HashMap<>();

    public static <T> void register(Class<T> clazz, Codec<T> codec) {
        REGISTRY.put(clazz, codec);
    }

    @SuppressWarnings("unchecked")
    public static <T> BuilderCodec<T> create(Class<T> clazz, Supplier<T> creator) {
        var builder = BuilderCodec.builder(clazz, creator);

        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(CodecAnnotations.Field.class)) continue;

            field.setAccessible(true);
            CodecAnnotations.Field meta = field.getAnnotation(CodecAnnotations.Field.class);
            String key = meta.value().isEmpty() ? field.getName() : meta.value();

            Codec<?> baseCodec = resolveCodecForType(field.getType());
            builder = appendField(builder, field, key, baseCodec, meta);
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    public static <T extends JsonAsset<String>> AssetBuilderCodec<String, T> createAsset(
            Class<T> clazz,
            Supplier<T> creator,
            Field idField,
            Field dataField
    ) {
        idField.setAccessible(true);
        dataField.setAccessible(true);
        var builder = AssetBuilderCodec.builder(
                clazz,
                creator,
                Codec.STRING,
                (instance, id) -> { try { idField.set(instance, id); } catch (Exception e) { throw new RuntimeException(e); } },
                (instance) -> { try { return (String) idField.get(instance); } catch (Exception e) { throw new RuntimeException(e); } },
                (instance, data) -> { try { dataField.set(instance, data); } catch (Exception e) { throw new RuntimeException(e); } },
                (instance) -> { try { return (AssetExtraInfo.Data) dataField.get(instance); } catch (Exception e) { throw new RuntimeException(e); } }
        );

        List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            allFields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }

        for (Field field : allFields) {
            if (!field.isAnnotationPresent(CodecAnnotations.Field.class)) continue;

            field.setAccessible(true);
            CodecAnnotations.Field meta = field.getAnnotation(CodecAnnotations.Field.class);
            String key = meta.value().isEmpty() ? field.getName() : meta.value();

            Codec<Object> baseCodec = (Codec<Object>) resolveCodecForType(field.getType());

            var context = builder.appendInherited(
                    new KeyedCodec<>(key, baseCodec),
                    (instance, value) -> { try { field.set(instance, value); } catch (Exception e) { throw new RuntimeException(e); } },
                    (instance) -> { try { return field.get(instance); } catch (Exception e) { throw new RuntimeException(e); } },
                    (instance, parent) -> { try { field.set(instance, field.get(parent)); } catch (Exception e) { throw new RuntimeException(e); } }
            );

            if (!meta.doc().isEmpty()) context.documentation(meta.doc());

            if (field.isAnnotationPresent(CodecAnnotations.CustomValidator.class)) {
                String validatorFieldName = field.getAnnotation(CodecAnnotations.CustomValidator.class).value();
                try {
                    Class<?> validatorClass = dev.rm20.anglersalmanac.Utils.Validator.CustomAssetValidator.class;
                    Field validatorField = validatorClass.getDeclaredField(validatorFieldName);
                    validatorField.setAccessible(true);
                    Object validatorInstance = validatorField.get(null);
                    if (validatorInstance instanceof com.hypixel.hytale.codec.validation.Validator) {
                        context.addValidator((com.hypixel.hytale.codec.validation.Validator<Object>) validatorInstance);
                    }
                } catch (Exception ignored) {}
            }

            builder = context.add();
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static <V, T> BuilderCodec.Builder<T> appendField(
            BuilderCodec.Builder<T> builder,
            Field field,
            String key,
            Codec<V> codec,
            CodecAnnotations.Field meta
    ) {
        BuilderField.FieldBuilder<T, V, BuilderCodec.Builder<T>> context = builder.append(
                new KeyedCodec<>(key, codec),
                (instance, value) -> {
                    try { field.set(instance, value); }
                    catch (Exception e) { throw new RuntimeException(e); }
                },
                (instance) -> {
                    try { return (V) field.get(instance); }
                    catch (Exception e) { throw new RuntimeException(e); }
                }
        );

        if (!meta.doc().isEmpty()) {
            context.documentation(meta.doc());
        }

        if (field.isAnnotationPresent(CodecAnnotations.Min.class)) {
            int minVal = field.getAnnotation(CodecAnnotations.Min.class).value();
            context.addValidator((Validator<V>) Validators.min(minVal));
        }
        if (field.isAnnotationPresent(CodecAnnotations.UniqueArray.class)) {
            context.addValidator((Validator<V>) Validators.uniqueInArray());
        }

        if (field.isAnnotationPresent(CodecAnnotations.NonNull.class)) {
            context.addValidator(Validators.nonNull());
        }

        if (field.isAnnotationPresent(CodecAnnotations.NonEmpty.class)) {
            context.addValidator((Validator<V>) Validators.nonEmptyString());
        }

        if (field.isAnnotationPresent(CodecAnnotations.CustomValidator.class)) {
            String validatorFieldName = field.getAnnotation(CodecAnnotations.CustomValidator.class).value();
            try {
                Class<?> validatorClass = dev.rm20.anglersalmanac.Utils.Validator.CustomAssetValidator.class;
                java.lang.reflect.Field validatorField = validatorClass.getDeclaredField(validatorFieldName);
                validatorField.setAccessible(true);

                Object validatorInstance = validatorField.get(null);

                if (validatorInstance instanceof Validator) {
                    context.addValidator((com.hypixel.hytale.codec.validation.Validator<V>) validatorInstance);
                }
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("Could not find a CustomAssetValidator named: " + validatorFieldName, e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access CustomAssetValidator: " + validatorFieldName, e);
            }
        }
        return context.add();
    }

    private static Codec<?> resolveCodecForType(Class<?> type) {
        // Primitives
        if (type == String.class) return Codec.STRING;
        if (type == Integer.TYPE || type == Integer.class) return Codec.INTEGER;
        if (type == Boolean.TYPE || type == Boolean.class) return Codec.BOOLEAN;
        if (type == Float.TYPE || type == Float.class) return Codec.FLOAT;
        if (type == Double.TYPE || type == Double.class) return Codec.DOUBLE;
        if (type == Long.TYPE || type == Long.class) return Codec.LONG;
        if (type == Byte.TYPE || type == Byte.class) return Codec.BYTE;
        if (type == Short.TYPE || type == Short.class) return Codec.SHORT;

        // Utility
        if (type == java.util.UUID.class) return Codec.UUID_STRING;
        if (type == java.nio.file.Path.class) return Codec.PATH;
        if (type == java.time.Instant.class) return Codec.INSTANT;
        if (type == java.time.Duration.class) return Codec.DURATION;
        if (type == com.hypixel.hytale.protocol.Color.class) {
            return com.hypixel.hytale.server.core.codec.ProtocolCodecs.COLOR;
        }

        if (type == String[].class) return Codec.STRING_ARRAY;
        if (type == int[].class) return Codec.INT_ARRAY;
        if (type == float[].class) return Codec.FLOAT_ARRAY;
        if (type == double[].class) return Codec.DOUBLE_ARRAY;
        if (type == long[].class) return Codec.LONG_ARRAY;
        if (type == byte[].class) return Codec.BYTE_ARRAY;
        if (type == Integer[].class) return new ArrayCodec<>(Codec.INTEGER, Integer[]::new);
        if (type == Float[].class) return new ArrayCodec<>(Codec.FLOAT, Float[]::new);
        if (type == Double[].class) return new ArrayCodec<>(Codec.DOUBLE, Double[]::new);
        if (type == Long[].class) return new ArrayCodec<>(Codec.LONG, Long[]::new);
        // Custom
        if (REGISTRY.containsKey(type)) {
            return REGISTRY.get(type);
        }

        // Auto-detect Enums
        if (type.isEnum()) {
            return new EnumCodec<>((Class<? extends Enum>) type);
        }

        // Auto-detect Objects mapping Enum Arrays
        if (type.isArray() && type.getComponentType().isEnum()) {
            Class<? extends Enum> enumClass = (Class<? extends Enum>) type.getComponentType();
            return new ArrayCodec<>(new EnumCodec<>(enumClass), size -> (Enum[]) java.lang.reflect.Array.newInstance(enumClass, size));
        }

        // Fallback for custom objects
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            if (REGISTRY.containsKey(componentType)) {
                Codec<Object> elementCodec = (Codec<Object>) REGISTRY.get(componentType);
                return new ArrayCodec<Object>(
                        elementCodec,
                        size -> (Object[]) java.lang.reflect.Array.newInstance(componentType, size)
                );
            }
        }

        throw new IllegalArgumentException("Registry missing mapped conversion format for Type: " + type.getName());
    }
}