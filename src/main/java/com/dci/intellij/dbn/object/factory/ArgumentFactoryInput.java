package com.dci.intellij.dbn.object.factory;

import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.Getter;

import java.util.List;


@Getter
public class ArgumentFactoryInput extends ObjectFactoryInput{

    private final String dataType;
    private final boolean input;
    private final boolean output;

    public ArgumentFactoryInput(ObjectFactoryInput parent, int index, String objectName, String dataType, boolean input, boolean output) {
        super(objectName, DBObjectType.ARGUMENT, parent, index);
        this.dataType = dataType == null ? "" : dataType.trim();
        this.input = input;
        this.output = output;
    }

    @Override
    public void validate(List<String> errors) {
        if (getObjectName().length() == 0) {
            errors.add("argument name is not specified at index " + getIndex());

        } else if (!Strings.isWord(getObjectName())) {
            errors.add("invalid argument name specified at index " + getIndex() + ": \"" + getObjectName() + "\"");
        }

        if (dataType.length() == 0){
            if (getObjectName().length() > 0) {
                errors.add("missing data type for argument \"" + getObjectName() + "\"");
            } else {
                errors.add("missing data type for argument at index " + getIndex());
            }
        }
    }
}
