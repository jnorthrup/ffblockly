const fs = require('fs');
const path = require('path');
const sandboxManager = require('./sandbox_manager');
const commandGenerator = require('./command_generator');
const { Filter, FilterGraph } = require('./filtergraph'); // Import classes for instantiation

const METADATA_FILENAME = 'metadata.json';

// Helper function to sanitize sandbox names (can be imported from sandbox_manager if it's exported, or duplicated)
// For now, let's assume sandbox_manager does not export it, so we define it or a similar utility.
// Or, even better, rely on sandbox_manager to always return/use sanitized names if possible.
// For this implementation, we'll assume sandboxName passed to these functions is the "true" (potentially sanitized) name.
// sandbox_manager.SANDBOXES_DIR will give the base path.

function getMetadataPath(sandboxName) {
  // Assuming sandboxName is the actual directory name (already sanitized if needed)
  return path.join(sandboxManager.SANDBOXES_DIR, sandboxName, METADATA_FILENAME);
}

function setExperimentMetadata(sandboxName, metadataObject) {
  // Ensure the sandbox directory itself exists.
  // sandboxManager.createSandbox would have made the directory.
  // If we're setting metadata for a sandbox not yet fully created by sandboxManager,
  // this might be an issue. Let's assume sandbox exists.
  const sandboxPath = path.join(sandboxManager.SANDBOXES_DIR, sandboxName);
  if (!fs.existsSync(sandboxPath)) {
    return { success: false, error: `Sandbox directory ${sandboxName} does not exist.` };
  }
  if (!metadataObject.createdAt) {
    metadataObject.createdAt = new Date().toISOString();
  }
  if (!metadataObject.status) {
    metadataObject.status = 'experimental'; // Default status
  }
   if (!metadataObject.name) {
    metadataObject.name = sandboxName; // Default name
  }


  const metadataFilePath = getMetadataPath(sandboxName);
  try {
    fs.writeFileSync(metadataFilePath, JSON.stringify(metadataObject, null, 2));
    return { success: true, path: metadataFilePath };
  } catch (err) {
    console.error(`Error writing metadata for sandbox "${sandboxName}":`, err);
    return { success: false, error: err.message };
  }
}

function getExperimentMetadata(sandboxName) {
  const metadataFilePath = getMetadataPath(sandboxName);

  if (!fs.existsSync(metadataFilePath)) {
    // To differentiate from a read error, check if sandbox dir exists
    const sandboxDir = path.dirname(metadataFilePath);
    if (!fs.existsSync(sandboxDir)) {
        return { success: false, error: `Sandbox ${sandboxName} not found.` };
    }
    return { success: false, error: `Metadata file not found for sandbox "${sandboxName}".` };
  }

  try {
    const fileContent = fs.readFileSync(metadataFilePath, 'utf8');
    const metadata = JSON.parse(fileContent);
    return { success: true, metadata: metadata };
  } catch (err) {
    console.error(`Error reading or parsing metadata for sandbox "${sandboxName}":`, err);
    return { success: false, error: err.message };
  }
}

function updateExperimentStatus(sandboxName, newStatus) {
  const result = getExperimentMetadata(sandboxName);
  if (!result.success) {
    return result; // Propagate the error
  }
  const metadata = result.metadata;
  metadata.status = newStatus;
  metadata.updatedAt = new Date().toISOString();

  // Use setExperimentMetadata to write it back
  return setExperimentMetadata(sandboxName, metadata);
}

// Helper function to convert plain filter graph object to FilterGraph instance
function hydrateFilterGraph(plainGraph) {
  if (!plainGraph || !Array.isArray(plainGraph.filters)) {
    throw new Error('Invalid plain graph object for hydration.');
  }
  const fg = new FilterGraph();
  plainGraph.filters.forEach(f => {
    // Assuming Filter constructor takes (name, options, inputs, outputs)
    // And that plainGraph.filters objects match this structure.
    const filterInstance = new Filter(f.name, f.options, f.inputs, f.outputs);
    fg.addFilter(filterInstance);
  });
  if (Array.isArray(plainGraph.connections)) {
    fg.connections = JSON.parse(JSON.stringify(plainGraph.connections)); // Deep copy
  }
  return fg;
}


function runExperiment(sandboxName, ffmpegPath = 'ffmpeg', inputFilePath = 'input.mp4', outputFilePath = 'output.mp4') {
  const filterGraphResult = sandboxManager.getSandboxFilterGraph(sandboxName);
  if (!filterGraphResult.success) {
    return { success: false, error: `Failed to get filter graph: ${filterGraphResult.error}` };
  }

  let filterGraphInstance;
  try {
    // The filterGraphObject from sandboxManager.getSandboxFilterGraph is a plain object.
    // command_generator.generateFfmpegCommand expects an instance of FilterGraph.
    filterGraphInstance = hydrateFilterGraph(filterGraphResult.filterGraph);
  } catch (err) {
    console.error(`Error hydrating filter graph for sandbox "${sandboxName}":`, err);
    return { success: false, error: `Could not process filter graph structure: ${err.message}` };
  }

  let filterComplexString;
  try {
    filterComplexString = commandGenerator.generateFfmpegCommand(filterGraphInstance);
  } catch (err) {
    console.error(`Error generating ffmpeg command for sandbox "${sandboxName}":`, err);
    return { success: false, error: `Command generation failed: ${err.message}` };
  }

  // Sanitize file paths for command line usage (basic example, might need more robust solution)
  const escapeShellArg = (arg) => `'${arg.replace(/'/g, "'\\''")}'`;

  const fullCommand = `${escapeShellArg(ffmpegPath)} -i ${escapeShellArg(inputFilePath)} -filter_complex ${escapeShellArg(filterComplexString)} ${escapeShellArg(outputFilePath)}`;

  console.log("Generated ffmpeg command:");
  console.log(fullCommand);
  console.log(`Conceptual execution: Would run ffmpeg command now for sandbox "${sandboxName}".`);

  // Update metadata to indicate it has been run (optional, or could be a specific status)
  const metadataResult = getExperimentMetadata(sandboxName);
  if (metadataResult.success) {
    let metadata = metadataResult.metadata;
    metadata.lastRunCommand = fullCommand;
    metadata.lastRunAt = new Date().toISOString();
    setExperimentMetadata(sandboxName, metadata); // Update metadata with run info
  }


  return { success: true, command: fullCommand };
}

module.exports = {
  setExperimentMetadata,
  getExperimentMetadata,
  updateExperimentStatus,
  runExperiment,
  METADATA_FILENAME
};
