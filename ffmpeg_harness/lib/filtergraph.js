class Filter {
  constructor(name, options, inputs, outputs) {
    this.name = name;
    this.options = options || {};
    this.inputs = inputs || [];
    this.outputs = outputs || [];
  }
}

class FilterGraph {
  constructor() {
    this.filters = [];
    this.connections = []; // Example: { from: { filterIndex: 0, padLabel: 'out' }, to: { filterIndex: 1, padLabel: 'in' } }
  }

  addFilter(filterInstance) {
    this.filters.push(filterInstance);
    return this.filters.length - 1; // Return the index of the added filter
  }

  connectFilters(outputFilterIndex, outputPadLabel, inputFilterIndex, inputPadLabel) {
    this.connections.push({
      from: { filterIndex: outputFilterIndex, padLabel: outputPadLabel },
      to: { filterIndex: inputFilterIndex, padLabel: inputPadLabel },
    });
  }
}

module.exports = { Filter, FilterGraph };
