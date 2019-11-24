package ffblockly.meta;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static ffblockly.meta.FFBlocklyInputValueBuilder.FFBFieldType.*;

public interface FFBlocklyInputValueBuilder {
    static FFBlocklyFieldValue createFFBlocklyInputValue(AvOption avOption) {
        return Scan.MYFACTORY.fil(new FFBlocklyFieldValue() {
            @Override
            public String getValue() {
                return field_number == getType() ? avOption.getDef() : null;
            }

            @Override
            public String getText() {
                String def = avOption.getDef();
                return field_input == getType() ? def == null ? "" : def : null;
            }


            @Override
            public String getMin() {
                return avOption.getRange1();
            }

            @Override
            public String getMax() {
                return avOption.getRange2();
            }

            @Override
            public Boolean getChecked() {
                return field_checkbox == getType() && avOption.getDef().equals(true) ? true : null;
            }

            @Override
            public FFBFieldType getType() {

                List<AvOption> children = avOption.getChildren();
                boolean b = null != children && !children.isEmpty();
                if (b)
                    return field_dropdown;
                for (FFBFieldType FFBFFBFieldType : values()) {
                    String type = FFBlockly.typesMap.getOrDefault(avOption.getType(), avOption.getType());

                    if (Objects.equals(FFBFFBFieldType.scanfor, type))
                        return FFBFFBFieldType;
                }
                return field_input;
            }


            @Override
            public List<List<String>> getOptions() {
                if (null == avOption.getChildren()) return null;
                else {
                    List<List<String>> collect;
                    collect = avOption.getChildren().stream().map(avOption1 -> asList(avOption1.getOptionName() + "\t|\t" + (null == avOption1.getDescription() ? "tbd" :
                            avOption1.getDescription()), avOption1.getOptionName())).collect(Collectors.toList());
                    collect.add(0, asList("", ""));
                    return collect;
                }
            }

            @Override
            public String getName() {
                return avOption.getOptionName();
            }

            @Override
            public String getTooltip() {
                return avOption.getDescription();
            }

            @Override
            public String getCheck() {
                return /*FFBlockly.typesMap.getOrDefault(avOption.getType(), avOption.getType())*/"String";
            }
        }).as();
    }

    enum FFBFieldType {
        field_input("String"),
        field_number("Number"),
        field_checkbox("Boolean"),
        field_colour("color"),
        field_dropdown("sdfalsjkdhfjahsf"),
        input_statement("asdasdasdasds"),
        input_value("sgddfg"),
        input_dummy("asdasdasd");
        private String scanfor;

        FFBFieldType(String scanfor) {
            this.scanfor = scanfor;
        }

    }
}