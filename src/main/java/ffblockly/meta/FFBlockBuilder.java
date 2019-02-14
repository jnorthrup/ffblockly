package ffblockly.meta;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Stream.concat;
import static ffblockly.meta.FFBlocklyInputValueBuilder.FFBFieldType.input_statement;

public interface FFBlockBuilder {

    int ORDINAL = input_statement.ordinal();

    static FFBlock createFFBlock(final FilterListing filterListing) {
        return Scan.MYFACTORY.block(new FFBlock() {
            boolean loaded;
            private List<AvOption> options;

            @Override
            public Boolean isInputsInline() {
                return true;
            }

            @Override
            public FilterListing.FilterSignature getPreviousStatement() {
                return filterListing.getInputSignature().iterator().hasNext() ? filterListing.getInputSignature().get(0) : null;
            }

            @Override
            public FilterListing.FilterSignature getNextStatement() {
                return filterListing.getReturnSignature().iterator().hasNext() ? filterListing.getReturnSignature().get(0) : null;
            }

            @Override
            public int getColour() {
                final List<FilterListing.FilterSignature> inputSignature = filterListing.getInputSignature() ;
                return  inputSignature.isEmpty()?0:FFBlockly.colorForString(String.valueOf(inputSignature.iterator().next()));
            }

            @Override
            public String getTooltip() {
                return filterListing.getShortDescription();
            }

            @Override
            public String getHelpUrl() {
                return MessageFormat.format("https://ffmpeg.org/ffmpeg-filters.html#{0}", filterListing.getFilterName());
            }

            @Override
            public String getType() {
                return filterListing.getFilterName();
            }

            @Override
            public String getMessage0() {
                final StringBuilder msg0 = new StringBuilder(this.getType());
                final List<FFBlocklyFieldValue> args0 = this.getArgs0();
                boolean first = true;

                if (null != args0) for (int i = 0; i < args0.size(); i++) {
                     FFBlocklyFieldValue theArg = args0.get(i);

                    if (null != theArg.getType()) {
                        FFBlocklyInputValueBuilder.FFBFieldType type = theArg.getType();
                        if (type.ordinal() < input_statement.ordinal()) {
                            msg0.append(first ? "=" : ":").append(theArg.getName()).append("=%").append(i + 1);
                            first = false;
                        }
                    }
                }
                return msg0.toString();
            }

            @Override
            public List<FFBlocklyFieldValue> getArgs0() {

                int[] c = {1};

                final Function<FilterListing.FilterSignature, FFBlocklyFieldValue> filterSignatureFFBlocklyFieldValueFunction = filterSignature -> FFBlockBuilder.getFfBlocklyFieldValue(c, filterSignature);

                final Stream<FFBlocklyFieldValue> stream1 = filterListing.getInputSignature().stream().skip(1).map(filterSignatureFFBlocklyFieldValueFunction);


                if (!this.loaded) {
                    this.loaded = true;
                    this.options = filterListing.getOptions();
                }
                c[0] = 1;
                final List<FFBlocklyFieldValue> collect = null;
                Stream<FFBlocklyFieldValue> ffBlocklyFieldValueStream = null == this.options ? new ArrayList<FFBlocklyFieldValue>().stream() : this.options.stream().map(FFBlocklyInputValueBuilder::createFFBlocklyInputValue);

                final List<FFBlocklyFieldValue> ret = concat(stream1, concat(concat(ffBlocklyFieldValueStream, asList(Scan.MYFACTORY.fil(new FFBlocklyFieldValue() {
                    @Override
                    public String getValue() {
                        return null;
                    }

                    @Override
                    public FFBlocklyInputValueBuilder.FFBFieldType getType() {
                        return FFBlocklyInputValueBuilder.FFBFieldType.input_dummy;
                    }

                    @Override
                    public String getText() {
                        return null;
                    }

                    @Override
                    public String getMin() {
                        return null;
                    }

                    @Override
                    public String getMax() {
                        return null;
                    }

                    @Override
                    public Boolean getChecked() {
                        return null;
                    }

                    @Override
                    public List<List<String>> getOptions() {
                        return null;
                    }

                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public String getTooltip() {
                        return null;
                    }

                    @Override
                    public String getCheck() {
                        return null;
                    }
                }).as()).stream()), filterListing.getReturnSignature().stream().skip(1).map(filterSignatureFFBlocklyFieldValueFunction))).collect(Collectors.toList());


                return ret.isEmpty() ? null : ret;
            }
        }).as();
    }

    static FFBlocklyFieldValue getFfBlocklyFieldValue(int[] c, FilterListing.FilterSignature filterSignature) {
        c[0]++;
        return Scan.MYFACTORY.fil(new FFBlocklyFieldValue() {
            @Override
            public String getValue() {
                return null;
            }

            @Override
            public FFBlocklyInputValueBuilder.FFBFieldType getType() {
                return input_statement;
            }

            @Override
            public String getText() {
                return null;
            }

            @Override
            public String getMin() {
                return null;
            }

            @Override
            public String getMax() {
                return null;
            }

            @Override
            public Boolean getChecked() {
                return null;
            }

            @Override
            public List<List<String>> getOptions() {
                return null;
            }

            @Override
            public String getName() {
                return "Input" + filterSignature.name() + c[0]++;
            }

            @Override
            public String getTooltip() {
                return null;
            }

            @Override
            public String getCheck() {
                return null;
            }
        }).as();
    }
}