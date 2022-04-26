package com.cavetale.editor.menu;

import com.cavetale.mytems.Mytems;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public enum NodeType {
    STRING(Category.PRIMITIVE,
           () -> Mytems.DATA_STRING.createIcon(),
           () -> "",
           c -> c == String.class,
           Function.identity()),
    INTEGER(Category.NUMBER,
            () -> Mytems.DATA_INTEGER.createIcon(),
            () -> 0,
            c -> c == Integer.class || c == int.class,
            Integer::parseInt),
    LONG(Category.NUMBER,
         () -> Mytems.DATA_INTEGER.createIcon(),
         () -> 0L,
         c -> c == Long.class || c == long.class,
         Long::parseLong),
    SHORT(Category.NUMBER,
          () -> Mytems.DATA_INTEGER.createIcon(),
          () -> (short) 0,
          c -> c == Short.class || c == short.class,
          Short::parseShort),
    DOUBLE(Category.NUMBER,
           () -> Mytems.DATA_FLOAT.createIcon(),
           () -> 0.0,
           c -> c == Double.class || c == double.class,
           Double::parseDouble),
    FLOAT(Category.NUMBER,
          () -> Mytems.DATA_FLOAT.createIcon(),
          () -> 0.0f,
          c -> c == Float.class || c == float.class,
          Float::parseFloat),
    BOOLEAN(Category.NUMBER,
            () -> Mytems.LETTER_B.createIcon(),
            () -> false,
            c -> c == Boolean.class || c == boolean.class,
            Boolean::parseBoolean),
    ENUM(Category.PRIMITIVE,
         () -> Mytems.LETTER_E.createIcon(),
         null,
         Class::isEnum,
         null),
    UUID(Category.PRIMITIVE,
         () -> new ItemStack(Material.PLAYER_HEAD),
         () -> new java.util.UUID(0L, 0L),
         java.util.UUID.class::isAssignableFrom,
         java.util.UUID::fromString),
    DATE(Category.PRIMITIVE,
         () -> new ItemStack(Material.CLOCK),
         Date::new,
         Date.class::isAssignableFrom,
         s -> {
             try {
                 return dateFormat().parse(s);
             } catch (ParseException pe) {
                 throw new IllegalArgumentException(pe);
             }
         }),
    MAP(Category.CONTAINER,
        () -> Mytems.FOLDER.createIcon(),
        () -> new HashMap<Object, Object>(),
        Map.class::isAssignableFrom,
        null),
    LIST(Category.CONTAINER,
         () -> Mytems.FOLDER.createIcon(),
         () -> new ArrayList<>(),
         List.class::isAssignableFrom,
         null),
    SET(Category.CONTAINER,
        () -> Mytems.FOLDER.createIcon(),
        () -> new HashSet<>(),
        Set.class::isAssignableFrom,
        null),
    OBJECT(Category.OBJECT,
           () -> Mytems.FOLDER.createIcon(),
           null,
           c -> true,
           null);

    public final Category category;
    public final Supplier<ItemStack> iconSupplier;
    public final Supplier<? extends Object> valueSupplier;
    public final Predicate<Class<?>> classPredicate;
    public final Function<String, ? extends Object> valueParser;
    public final String humanName = name().substring(0, 1) + name().substring(1).toLowerCase();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.S");

    private enum Category {
        NUMBER,
        PRIMITIVE,
        CONTAINER,
        OBJECT;
    }

    public boolean isMenu() {
        return category == Category.CONTAINER || category == Category.OBJECT;
    }

    public boolean isPrimitive() {
        return category == Category.PRIMITIVE || category == Category.NUMBER;
    }

    public boolean isNumber() {
        return category == Category.NUMBER;
    }

    public boolean isContainer() {
        return category == Category.CONTAINER;
    }

    public static NodeType of(Class<?> type) {
        for (NodeType it : values()) {
            if (it.classPredicate.test(type)) return it;
        }
        return OBJECT;
    }

    public boolean canParseValue() {
        return valueParser != null;
    }

    public Object parseValue(String in) throws IllegalArgumentException {
        if (valueParser == null) throw new IllegalArgumentException(this + " cannot be parsed!");
        return valueParser.apply(in);
    }

    public boolean canCreateNewInstance() {
        return valueSupplier != null;
    }

    public Object createNewInstance() {
        if (valueSupplier == null) throw new IllegalArgumentException(this + " cannot be created!");
        return valueSupplier.get();
    }

    private static SimpleDateFormat dateFormat() {
        return DATE_FORMAT;
    }

    public ItemStack createIcon() {
        return iconSupplier.get();
    }
}
