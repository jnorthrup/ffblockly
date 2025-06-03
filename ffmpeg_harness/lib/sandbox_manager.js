const fs = require('fs');
const path = require('path');

const SANDBOXES_DIR = path.join(__dirname, '..', 'ffmpeg_sandboxes');

// Helper function to sanitize sandbox names (optional, but good practice)
function sanitizeSandboxName(name) {
  return name.toString().toLowerCase().replace(/\s+/g, '_').replace(/[^a-z0-9_.-]/g, '');
}

// Ensure SANDBOXES_DIR exists - call this in functions that need it.
function ensureBaseDirExists() {
  if (!fs.existsSync(SANDBOXES_DIR)) {
    try {
      fs.mkdirSync(SANDBOXES_DIR, { recursive: true });
    } catch (err) {
      console.error(`Failed to create base sandbox directory: ${SANDBOXES_DIR}`, err);
      throw err; // Re-throw if critical, or handle appropriately
    }
  }
}

function createSandbox(sandboxName, filterGraphObject) {
  ensureBaseDirExists();
  const saneName = sanitizeSandboxName(sandboxName);
  if (!saneName) {
    console.error("Error: Sandbox name is invalid or empty after sanitization.");
    return { success: false, error: "Invalid sandbox name." };
  }
  const sandboxPath = path.join(SANDBOXES_DIR, saneName);

  if (fs.existsSync(sandboxPath)) {
    console.error(`Error: Sandbox "${saneName}" already exists at ${sandboxPath}`);
    return { success: false, error: `Sandbox "${saneName}" already exists.` };
  }

  try {
    fs.mkdirSync(sandboxPath);
    const filterGraphFilePath = path.join(sandboxPath, 'filtergraph.json');
    // Note: filterGraphObject should be a plain JS object for JSON.stringify.
    // If it's an instance of FilterGraph class, it might need a toJSON() method or manual conversion.
    // For this subtask, we assume filterGraphObject is already a serializable plain object.
    fs.writeFileSync(filterGraphFilePath, JSON.stringify(filterGraphObject, null, 2));
    return { success: true, path: sandboxPath };
  } catch (err) {
    console.error(`Error creating sandbox "${saneName}":`, err);
    // Attempt to clean up if directory was created but file writing failed
    if (fs.existsSync(sandboxPath) && !fs.existsSync(path.join(sandboxPath, 'filtergraph.json'))) {
        try {
            fs.rmdirSync(sandboxPath); // Remove the empty dir
        } catch (cleanupErr) {
            console.error(`Error cleaning up partially created sandbox "${saneName}":`, cleanupErr);
        }
    }
    return { success: false, error: err.message };
  }
}

function listSandboxes() {
  ensureBaseDirExists(); // Ensure base directory is checked/created before trying to read
  try {
    if (!fs.existsSync(SANDBOXES_DIR)) {
      // This case should ideally be handled by ensureBaseDirExists creating it,
      // but as a fallback or if ensureBaseDirExists fails silently and doesn't throw.
      return [];
    }
    const entries = fs.readdirSync(SANDBOXES_DIR, { withFileTypes: true });
    const sandboxNames = entries
      .filter(dirent => dirent.isDirectory())
      .map(dirent => dirent.name);
    return sandboxNames;
  } catch (err) {
    console.error("Error listing sandboxes:", err);
    // Depending on desired behavior, could return empty array or throw
    return { success: false, error: err.message, sandboxes: [] }; // Or simply return []
  }
}

function deleteSandbox(sandboxName) {
  // No need to call ensureBaseDirExists() here, as if SANDBOXES_DIR doesn't exist, the sandbox won't exist.
  const saneName = sanitizeSandboxName(sandboxName);
   if (!saneName) {
    console.error("Error: Sandbox name is invalid or empty for deletion.");
    return { success: false, error: "Invalid sandbox name for deletion." };
  }
  const sandboxPath = path.join(SANDBOXES_DIR, saneName);

  if (!fs.existsSync(sandboxPath)) {
    console.warn(`Warning: Sandbox "${saneName}" not found for deletion at ${sandboxPath}`);
    return { success: false, error: `Sandbox "${saneName}" not found.` };
  }

  try {
    // Assuming Node.js v14.14.0+ for fs.rmSync
    // For older versions, a recursive delete function would be needed.
    fs.rmSync(sandboxPath, { recursive: true, force: true });
    return { success: true };
  } catch (err) {
    console.error(`Error deleting sandbox "${saneName}":`, err);
    return { success: false, error: err.message };
  }
}

function getSandboxFilterGraph(sandboxName) {
  const saneName = sanitizeSandboxName(sandboxName);
  if (!saneName) {
    console.error("Error: Sandbox name is invalid or empty for retrieval.");
    return { success: false, error: "Invalid sandbox name for retrieval." };
  }
  const filterGraphFilePath = path.join(SANDBOXES_DIR, saneName, 'filtergraph.json');

  if (!fs.existsSync(filterGraphFilePath)) {
    // Check if the directory itself exists, to give a more specific error
    if (!fs.existsSync(path.join(SANDBOXES_DIR, saneName))) {
        console.warn(`Warning: Sandbox directory "${saneName}" not found.`);
        return { success: false, error: `Sandbox "${saneName}" not found.` };
    }
    console.warn(`Warning: Filtergraph file not found in sandbox "${saneName}" at ${filterGraphFilePath}`);
    return { success: false, error: `Filtergraph JSON not found in sandbox "${saneName}".` };
  }

  try {
    const fileContent = fs.readFileSync(filterGraphFilePath, 'utf8');
    const filterGraphObject = JSON.parse(fileContent);
    return { success: true, filterGraph: filterGraphObject };
  } catch (err) {
    console.error(`Error reading or parsing filtergraph for sandbox "${saneName}":`, err);
    return { success: false, error: err.message };
  }
}

module.exports = {
  createSandbox,
  listSandboxes,
  deleteSandbox,
  getSandboxFilterGraph,
  SANDBOXES_DIR // Exporting for testing or external use if needed
};
