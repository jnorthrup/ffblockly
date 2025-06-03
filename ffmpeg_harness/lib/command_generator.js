const { FilterGraph } = require('./filtergraph'); // Assuming FilterGraph is also exported if needed, or adjust path

function generateFfmpegCommand(filterGraphInstance) {
  if (!(filterGraphInstance instanceof FilterGraph)) {
    throw new Error("Invalid input: Expected an instance of FilterGraph.");
  }

  const filterStrings = [];
  const streamLabels = new Map(); // To keep track of output labels of filters

  // Initial input stream label
  let currentInputStreamLabel = '[0:v]';

  if (filterGraphInstance.filters.length === 0) {
    return ""; // No filters, empty command
  }

  // Simple linear chain assumption for now
  // This initial implementation assumes filters are added in order and chain directly.
  // e.g., output of filter 0 goes to input of filter 1, etc.
  // Connections array is not fully utilized yet for complex routing.

  filterGraphInstance.filters.forEach((filter, index) => {
    const inputPads = filter.inputs.map(pad => {
      // For the first filter, or if an input pad is explicitly '0:v' (or similar global input)
      if (index === 0 && filter.inputs.length === 1 && !streamLabels.has(`${index-1}_${pad}`)) {
        return currentInputStreamLabel;
      }
      // Try to find a connection to this filter's input pad
      const connection = filterGraphInstance.connections.find(
        c => c.to.filterIndex === index && c.to.padLabel === pad
      );
      if (connection) {
        const fromFilter = filterGraphInstance.filters[connection.from.filterIndex];
        // Construct the label from the source filter's output pad
        return `[${connection.from.filterIndex}_${connection.from.padLabel}]`;
      } else if (index > 0 && filter.inputs.length === 1) {
        // Simple linear chain: output of previous filter's default output pad
        const prevFilter = filterGraphInstance.filters[index-1];
        const prevFilterDefaultOutputPad = prevFilter.outputs[0] || 'out'; // Assume 'out' if not specified
         return `[${index-1}_${prevFilterDefaultOutputPad}]`;
      }
      return `[unconnected_input_${index}_${pad}]`; // Placeholder for unconnected inputs

    }).join('');

    const outputPads = filter.outputs.map(pad => `[${index}_${pad}]`).join('');
    streamLabels.set(index, filter.outputs.map(pad => `${index}_${pad}`));


    let optionsString = Object.entries(filter.options)
      .map(([key, value]) => `${key}=${value}`)
      .join(':');
    if (optionsString) {
      optionsString = `=${optionsString}`;
    }

    filterStrings.push(`${inputPads}${filter.name}${optionsString}${outputPads}`);
  });

  // Determine the final output label
  // For now, assumes the last filter's first output pad is the final output, or a explicitly defined connection to an 'out' sink
  let finalOutputLabel = '[out]'; // Default final output label
  if (filterGraphInstance.filters.length > 0) {
      const lastFilterIndex = filterGraphInstance.filters.length - 1;
      const lastFilter = filterGraphInstance.filters[lastFilterIndex];
      if (lastFilter.outputs.length > 0) {
          // Check if the last filter's output is connected to a generic output like 'outv' or 'out'
          const explicitOutConnection = filterGraphInstance.connections.find(c => {
              return c.from.filterIndex === lastFilterIndex && filterGraphInstance.filters[c.from.filterIndex].outputs.includes(c.from.padLabel) && c.to.padLabel === 'out';
          });

          if (explicitOutConnection) {
              finalOutputLabel = `[${explicitOutConnection.to.padLabel}]`;
              // Need to ensure the last filter string actually uses this label if it's a direct connection to a sink
              // This part might need refinement based on how ffmpeg expects sinks to be declared.
              // For now, we assume the last filter's output is implicitly the one we want to expose.
          } else {
              // Default to last filter's first output pad, but ensure it's not already connected to another filter
              const lastFilterOutputPad = lastFilter.outputs[0];
              const isLastFilterOutputConnectedInternally = filterGraphInstance.connections.some(
                  c => c.from.filterIndex === lastFilterIndex && c.from.padLabel === lastFilterOutputPad && c.to.filterIndex !== -1 // -1 or similar could denote final output
              );
              if (!isLastFilterOutputConnectedInternally) {
                  // Modify the last filter string to use the finalOutputLabel if it's not a simple [out]
                  // This is tricky because the output pad name is part of the filter string.
                  // For now, we rely on the user to ensure the last filter's output is correctly named if not '[out]'
                  // Or, the calling code would map this logical '[out]' to the actual output file.
                  // Let's assume for filter_complex, the last unconsumed output becomes the link to -map
                  // We will ensure the last filter string's output pad becomes the final output label if not otherwise specified.
                  const lastFilterOutputLabel = `[${lastFilterIndex}_${lastFilter.outputs[0]}]`;
                  if (filterStrings.length > 0) {
                      const lastFilterString = filterStrings[filterStrings.length -1];
                      // If the command is to be used with -filter_complex, the output of the last filter can be mapped.
                      // So, [lastFilterIndex_lastFilter.outputs[0]] is a valid label to be mapped by -map.
                      // If we want the final output of the chain to be literally '[out]', the last filter must declare it.
                      // For now, we'll stick to the convention that the last filter's primary output is the one of interest.
                  }
            }
        }
      }
  }


  // Join distinct filter chains with semicolons, and filters within a chain with commas.
  // This initial version assumes a single chain, so all are joined by commas.
  // A more sophisticated version would use the `connections` array to determine chains.
  // For now, if connections are made, they define the linkage. If not, it's a simple linear chain.

  // A simple approach for now:
  // 1. Iterate filters. For each filter, format it.
  // 2. If a filter's input is not connected from a previous filter's output (tracked via connections or naming convention),
  //    it's the start of a new chain (or uses a global input like [0:v]).
  // 3. Chains are joined by ';', filters within a chain by ','.

  // This version will just join all formatted filters by a comma, assuming one main chain or
  // that the connections correctly link everything into what ffmpeg sees as parts of a single complex filtergraph.
  let complexFilterGraphString = filterStrings.join(',');


  // This part is tricky: if the last filter's output is not explicitly named '[out]',
  // and we want the graph's output to be mapped from '[out]', we might need to add an extra step
  // or ensure the last filter's output label IS '[out]'.
  // For this version, we assume the calling context will handle mapping.
  // The `generateFfmpegCommand` will return the content for `-filter_complex "..."`.
  // Example: "[0:v]scale=w=640:h=360[scaled]; [scaled]setsar=1[out]"
  // The logic for semicolons (new chains) vs commas (filters in same chain) needs to be robust.

  // Let's refine the chain logic based on connections.
  // A filter is a start of a new chain if its inputs are not connected to any previous filter's outputs.
  // This is a simplified view. True chaining logic can be complex.

  // For now, the initial implementation will join all filters with a comma.
  // This works for linear chains. Semicolon usage for parallel chains is more advanced.
  // Example: `[0:v]split[main][tmp]; [tmp]crop=iw/2:ih:0:0[left]; [main]scale=iw/2:-1[right]; [left][right]hstack[out]`
  // The current code structure will produce something like:
  // "[0:v]filter1=opt[0_out], [0_out]filter2=opt[1_out], [1_out]filter3=opt[2_out]" (if linearly connected)

  // A more robust way to build the string considering connections:
  const processedEdges = new Set();
  let finalString = "";
  const filterDefinitions = [];

  // Pass 1: Define all filters and their explicit output labels
  filterGraphInstance.filters.forEach((filter, i) => {
    const inputPadsString = filter.inputs.map(pad => {
        // Attempt to find connection to this input pad
        const conn = filterGraphInstance.connections.find(c => c.to.filterIndex === i && c.to.padLabel === pad);
        if (conn) {
            const fromFilterOutputs = filterGraphInstance.filters[conn.from.filterIndex].outputs;
            // Ensure the from pad actually exists on the from filter
            if (fromFilterOutputs.includes(conn.from.padLabel)) {
                 return `[${conn.from.filterIndex}_${conn.from.padLabel}]`;
            }
        }
        // Default to global input if it's the first filter and no specific connection
        if (i === 0 && pad === (filter.inputs[0] || 'in')) return '[0:v]'; // Default for first filter
        return `[unconnected_${i}_${pad}]`; // Or handle as error
    }).join('');

    const outputPadsString = filter.outputs.map(pad => `[${i}_${pad}]`).join('');

    let opts = Object.entries(filter.options)
      .map(([k, v]) => `${k}=${v}`)
      .join(':');
    if (opts) opts = `=${opts}`;

    filterDefinitions[i] = `${inputPadsString}${filter.name}${opts}${outputPadsString}`;
  });

  // The problem with just joining filterDefinitions is that it doesn't respect ffmpeg's syntax
  // for chaining (commas for serial, semicolons for parallel/new chains).
  // The current connection model is explicit. We can build the graph string by joining
  // all filter definitions with ';'. Then, ffmpeg will link them based on the pad labels.
  // This is the most straightforward way with explicit labeling.

  finalString = filterDefinitions.join(';'); // Changed from ',' to ';'

  // Ensure the final output of the graph is labeled for mapping, e.g. to "[out]"
  // This might involve adding a final identity filter if the last filter's output isn't already named '[out]'
  // or if multiple outputs need to be mapped.
  // For now, we assume the calling code handles mapping from the last filter's actual output pad.
  // E.g., if last filter is `...[2_outv]`, then `-map "[2_outv]"` would be used.
  // If the goal is to always have a final `[out]` pad from this function,
  // we might need to add a final filter like `[2_outv]copy[out];` if `[out]` is desired.

  // Let's try to make the output always end with `[out]` if there's a designated final output.
  // Find the "final" output pad of the graph. This could be an unconnected output pad of the last filter,
  // or an explicitly defined output connection (e.g., to a virtual 'out' node).

  let hasExplicitOut = false;
  filterGraphInstance.connections.forEach(conn => {
      if (conn.to.padLabel === 'out' && conn.to.filterIndex === -1) { // Assuming -1 for graph output sink
          const fromFilter = filterGraphInstance.filters[conn.from.filterIndex];
          const fromPad = fromFilter.outputs.find(p => p === conn.from.padLabel);
          if (fromPad) {
              const sourceLabel = `[${conn.from.filterIndex}_${fromPad}]`;
              // If the sourceLabel is already the output of a filter definition, we might not need to do anything extra,
              // or we might need to ensure it's the *last* statement and properly linked.
              // This is simplified: if a filter's output is meant to be the final 'out',
              // we might need to append a 'copy' filter to rename its output pad to '[out]'.
              // e.g. if filter_defs end with ...[2_someout], and we want [out], add ";[2_someout]copy[out]"
              // This is only if the last filter doesn't already output to a pad named 'out' *and* that pad is correctly indexed.

              // For now, the simplest is to let ffmpeg connect by labels.
              // The user must ensure one of the output pads is eventually mapped.
              // If a specific '[out]' is required by generateFfmpegCommand itself:
              const lastFilter = filterGraphInstance.filters[filterGraphInstance.filters.length-1];
              const lastFilterDefaultOutputLabel = `${filterGraphInstance.filters.length-1}_${lastFilter.outputs[0]}`;
              if (`[${lastFilterDefaultOutputLabel}]` !== '[out]') {
                  // This logic is tricky: if the last filter already has an output named 'out' with its index, e.g. [2_out], it's fine.
                  // If we want the *graph* to resolve to a simple '[out]', we need to ensure that.
                  // The current filterDefinitions join with ';' should allow ffmpeg to resolve.
                  // Let's test this assumption first.
              }
              hasExplicitOut = true;
          }
      }
  });

  // The most robust way for ffmpeg is to define all filter segments separated by ';'.
  // ffmpeg then pieces them together using the input/output pad labels.
  // Example: "[0:v]scale=w=640:h=360[scaled]; [scaled]setsar=1[out]"
  // filterDefinitions[0] = "[0:v]scale=w=640:h=360[0_scaled]"
  // filterDefinitions[1] = "[0_scaled]setsar=1[1_out]"
  // joining with ';' -> "[0:v]scale=w=640:h=360[0_scaled];[0_scaled]setsar=1[1_out]"
  // This is correct. The labels link them.

  return filterDefinitions.join(';');
}

module.exports = { generateFfmpegCommand };
