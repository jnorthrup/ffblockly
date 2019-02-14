package ffblockly.meta;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import ffblockly.meta.FilterListing.FilterSignature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static ffblockly.meta.FilterListingBuilder.createFilterListing;

public class Scan {

    public static final FFFactory MYFACTORY = AutoBeanFactorySource.create(FFFactory.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        int c = 0;
        Path ffOut = Files.createTempFile("ffscanXXXX", ".out");
        Path ffErr = Files.createTempFile("ffscanXXXX", ".err");
        Process start = new ProcessBuilder().command("ffmpeg", "-filters").redirectError(ffErr.toFile()).redirectOutput(ffOut.toFile()).start();

        ffOut.toFile().deleteOnExit();
        ffErr.toFile().deleteOnExit();
        int i = start.waitFor();
        System.err.println("exit: " + i);
        List<String> strings = Files.readAllLines(ffOut);
        AutoBean<FilterModel> model = Scan.MYFACTORY.model(() -> strings.stream().filter(FilterListing.FILTERS_LIST_FORMAT.asPredicate()).map(s -> {
            Matcher matcher = FilterListing.FILTERS_LIST_FORMAT.matcher(s);
            matcher.matches();
            return createFilterListing(matcher);
        }).collect(Collectors.toList()));

        FilterModel filterModel1 = model.as();
        FFBlockly ffBlockly = Scan.MYFACTORY.blockly(new FFBlockly() {

            public FilterModel filterModel = filterModel1;

            @Override
            public Set<FFBlock> getModel() {
                return this.filterModel.getFilters().stream().map(FFBlockBuilder::createFFBlock).collect(Collectors.toCollection(() -> new TreeSet<>((o1, o2) -> o1.getType().compareTo(o2.getType()))));
            }
        }).as();
        System.out.println(AutoBeanCodex.encode(Scan.MYFACTORY.blockly(ffBlockly)).getPayload());
    }

    private static void testFilter(FilterListing filterListing) {
        System.out.println(filterListing.getCommandSupport());
        System.out.println(filterListing.getFilterName());
        List<FilterSignature> inputSignature = filterListing.getInputSignature();
        System.out.println("a: " + inputSignature);
        List<FilterSignature> returnSignature = filterListing.getReturnSignature();
        System.out.println("b: " + returnSignature);
        System.out.println(filterListing.getShortDescription());
        System.out.println(filterListing.getSliceThreading());
        for (AvOption avOption : filterListing.getOptions()) {
            System.out.println(avOption.getType());
            System.out.println(avOption.getAudio());
            System.out.println(avOption.getChildren());
            System.out.println(avOption.getDecoding());
            System.out.println(avOption.getDef());
            System.out.println(avOption.getTargets());
            System.out.println(avOption.getEncoding());
            System.out.println(avOption.getExport());
            System.out.println(avOption.getFiltering());
            System.out.println(avOption.getName());
            System.out.println(avOption.getRange1());
            System.out.println(avOption.getRange2());
            System.out.println(avOption.getReadonly());
            System.out.println(avOption.getSubtitle());
        }
    }

    /**
     *
     */
    // Declare the factory type
    interface FFFactory extends AutoBeanFactory {
        AutoBean<FilterListing> listing();

        AutoBean<FilterListing> listing(FilterListing wrap);

        AutoBean<AvOption> options();

        AutoBean<AvOption> options(AvOption wrap);

        AutoBean<FilterModel> model();

        AutoBean<FilterModel> model(FilterModel filterModel);

        AutoBean<FFBlock> block();

        AutoBean<FFBlock> block(FFBlock ffblock);

        AutoBean<FFBlockly> blockly();

        AutoBean<FFBlockly> blockly(FFBlockly x);

        AutoBean<FFBlocklyFieldValue> fil();

        AutoBean<FFBlocklyFieldValue> fil(FFBlocklyFieldValue x);

    }

}

