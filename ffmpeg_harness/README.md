# FFmpeg Harness CLI

## Overview

FFmpeg Harness CLI is a Node.js application designed to help manage and run ffmpeg filtergraph experiments. It provides a structured way to define complex video and audio processing pipelines using JSON, store them in "sandboxes," and generate the corresponding ffmpeg command-line arguments. This tool aims to simplify the creation, testing, and iteration of ffmpeg filtergraphs.

## Features

*   **Sandbox Management:** Create, list, show, and delete sandboxes, where each sandbox holds a filtergraph definition and its metadata.
*   **JSON Filtergraph Definition:** Define ffmpeg filtergraphs using a clear JSON structure, specifying filters, their options, and their input/output connections.
*   **Command Generation:** Conceptually generate ffmpeg `filter_complex` command strings from the filtergraph definitions. (Actual execution is stubbed in the current version).
*   **Experiment Metadata:** Store and manage metadata for each experiment/sandbox, including a user-friendly name, description, creation date, and status.
*   **Status Tracking:** Update the status of experiments (e.g., "experimental", "keep", "archived").
*   **Command-Line Interface (CLI):** Interact with all features through an intuitive CLI built with `yargs`.

## Project Structure

```
ffmpeg_harness/
├── ffmpeg-harness-cli.js   # Main CLI executable script
├── lib/                    # Core library modules
│   ├── filtergraph.js        # Defines Filter and FilterGraph classes
│   ├── command_generator.js  # Generates ffmpeg filter_complex strings
│   ├── sandbox_manager.js    # Manages sandbox creation, deletion, and file I/O
│   └── experiment_manager.js # Manages experiment metadata and conceptual runs
├── ffmpeg_sandboxes/       # Default directory for storing all sandboxes
│   └── my_sandbox_name/    # Example sandbox directory
│       ├── filtergraph.json  # Filtergraph definition for this sandbox
│       └── metadata.json     # Metadata for this sandbox
├── node_modules/           # Project dependencies (managed by npm)
├── package.json            # Project manifest, dependencies, and scripts
├── package-lock.json       # Records exact versions of dependencies
└── README.md               # This documentation file
```

## Installation/Setup

1.  **Prerequisites:**
    *   Node.js (v18.x or higher recommended, see `yargs` engine warnings for specifics)
    *   npm (usually comes with Node.js)

2.  **Clone the Repository (if applicable):**
    ```bash
    # git clone <repository_url>
    # cd ffmpeg_harness
    ```

3.  **Install Dependencies:**
    Navigate to the `ffmpeg_harness` project root directory and run:
    ```bash
    npm install
    ```
    This will install `yargs` and any other dependencies listed in `package.json`.

4.  **Running/Linking the CLI:**
    After installation, you can run the CLI directly:
    ```bash
    node ./ffmpeg-harness-cli.js <command> [options]
    ```
    Or, to make it available as a system command (e.g., `ffmpeg-harness`), you can link it using npm:
    ```bash
    cd ffmpeg_harness
    npm link
    ```
    Now you should be able to run `ffmpeg-harness <command> [options]` from any directory.

## Defining Filtergraphs

Filtergraphs are defined in JSON format and stored within each sandbox directory in a file named `filtergraph.json`.

### JSON Structure

A filtergraph object primarily contains an array of `filters` and an array of `connections`.

*   **`Filter` Object:**
    *   `name` (string): The name of the ffmpeg filter (e.g., "scale", "setsar", "split").
    *   `options` (object): A key-value map of filter options (e.g., `{ "w": "1280", "h": "720" }`).
    *   `inputs` (array of strings): Labels for input pads. These labels should match output pad labels from preceding filters or global input stream specifiers (e.g., "0:v", "0:a").
    *   `outputs` (array of strings): Labels for output pads. These labels are used by subsequent filters to connect to this filter's outputs. The `command_generator.js` will internally prefix these with the filter's index to create unique stream labels for ffmpeg (e.g., `[0_scaled_vid]`).

*   **`FilterGraph` Object:**
    *   `filters` (array): An ordered array of `Filter` objects. The order determines the primary processing sequence.
    *   `connections` (array): An array of connection objects, used for defining more complex or non-linear routing between filter pads. Each connection object typically looks like:
        `{ "from": { "filterIndex": 0, "padLabel": "out_pad" }, "to": { "filterIndex": 1, "padLabel": "in_pad" } }`
        *   **Note:** The current `command_generator.js` primarily relies on implicit linear chaining by matching a filter's `inputs` array content to the `outputs` labels of previous filters in the `filters` array. The `connections` array is parsed but its advanced routing capabilities (like true non-linear graphs) are more fully realized when `command_generator.js` is enhanced. For simple linear chains, explicitly defining matching input/output labels in the `filters` array is often sufficient, and the `connections` array might be empty or used to reinforce these links. The `command_generator.js` uses indexed output pad labels (e.g., `[filterIndex_padLabel]`) for linking when constructing the ffmpeg command.

### Example `filtergraph.json`

This example shows a simple two-filter chain: scaling a video, then setting its Sample Aspect Ratio (SAR).

```json
{
  "filters": [
    {
      "name": "scale",
      "options": { "w": "1280", "h": "720" },
      "inputs": ["0:v"],
      "outputs": ["scaled_vid"]
    },
    {
      "name": "setsar",
      "options": { "sar": "1/1" },
      "inputs": ["scaled_vid"],
      "outputs": ["final_vid"]
    }
  ],
  "connections": [
    {
      "from": { "filterIndex": 0, "padLabel": "scaled_vid" },
      "to": { "filterIndex": 1, "padLabel": "scaled_vid" }
    }
  ]
}
```
**Explanation of the example:**
1.  The `scale` filter (index 0) takes the main video input `[0:v]` (as declared in its `inputs` array) and produces an output stream conceptually labeled `scaled_vid`. The command generator will turn this into `[0_scaled_vid]`.
2.  The `setsar` filter (index 1) takes the output of the `scale` filter. Its `inputs` array specifies `scaled_vid`. The command generator will look for `[0_scaled_vid]` to connect to this input. It produces an output labeled `final_vid` (which becomes `[1_final_vid]`).
3.  The `connections` array explicitly states that the `scaled_vid` output of filter 0 connects to an input of filter 1. The `command_generator.js` uses these explicit connections to map the `inputs` of a filter (like `scaled_vid` for `setsar`) to the correctly indexed output stream from a previous filter (like `[0_scaled_vid]` from the `scale` filter). If the `inputs` array of a filter contains a label that matches an `outputs` label of a previous filter, this forms an implicit connection that the `hydrateFilterGraph` and `generateFfmpegCommand` functions resolve.

## CLI Usage

(Assumes CLI is linked or run via `node ./ffmpeg-harness-cli.js`)

*   **Create a new sandbox:**
    ```bash
    ffmpeg-harness create my_first_experiment --name "My Experiment" --description "Testing a scale and crop." --filtergraph-file ./path/to/mygraph.json
    # Or with JSON string:
    ffmpeg-harness create another_exp --filtergraph-json '{ "filters": [{"name":"scale","options":{"w":"640"},"inputs":["0:v"],"outputs":["s1"]}], "connections":[] }'
    ```

*   **List all sandboxes:**
    ```bash
    ffmpeg-harness list
    # Alias: ffmpeg-harness ls
    ```

*   **Show details for a sandbox:**
    ```bash
    ffmpeg-harness show my_first_experiment
    # Alias: ffmpeg-harness s my_first_experiment
    ```

*   **Run an experiment (generate conceptual command):**
    ```bash
    ffmpeg-harness run my_first_experiment --input video.mp4 --output processed_video.mp4
    # Alias: ffmpeg-harness r my_first_experiment
    ```

*   **Update the status of an experiment:**
    ```bash
    ffmpeg-harness status my_first_experiment keep
    ```

*   **Delete a sandbox:**
    ```bash
    ffmpeg-harness delete my_first_experiment
    # Aliases: ffmpeg-harness rm my_first_experiment, ffmpeg-harness del my_first_experiment
    # Add --force if implemented for non-interactive deletion (currently default behavior)
    ```

## Refining "Constructively Creating Joins"

The phrase "constructively creating joins" refers to the process by which this tool helps build complex ffmpeg filtergraphs. Instead of manually writing a long, error-prone `filter_complex` string with intricate pad labeling and connections, you:

1.  **Construct** your filter pipeline by defining a sequence of individual filters in a JSON object.
2.  For each filter, you specify its `name`, `options`, and importantly, its `inputs` and `outputs` using human-readable labels.
3.  The `command_generator.js` module then takes this structured definition. It programmatically translates your declared `inputs` and `outputs` into the precise, uniquely labeled input/output pads that ffmpeg requires (e.g., `[0_scaled_vid]`, `[1_processed_audio]`).
4.  It then **joins** these filters together into a valid `filter_complex` string, ensuring that the output of one filter correctly feeds into the input of the next, based on the connections you've defined (either implicitly by matching input/output labels or explicitly via the `connections` array).

This approach offers a more structured, less error-prone method for building these "joins" or connections in an ffmpeg filtergraph, abstracting away some of the complexities of ffmpeg's internal labeling syntax.

## Conceptual Nature of `ffblockly`

`ffblockly` appears to have been a conceptual project or tool aimed at providing a block-based graphical user interface (GUI) for constructing ffmpeg commands, similar to Google's Blockly visual programming editor.

This FFmpeg Harness CLI project is **not** an emulation or implementation of `ffblockly`'s GUI. Instead, it provides a backend and command-line interface for achieving a similar goal: structured, simplified creation and management of ffmpeg filter pipelines. While `ffblockly` focused on a visual approach, this project uses JSON definitions and a CLI for a developer-oriented workflow. Both aim to make ffmpeg's powerful filtergraph capabilities more accessible.

---
*This README was generated as part of a software engineering task.*
