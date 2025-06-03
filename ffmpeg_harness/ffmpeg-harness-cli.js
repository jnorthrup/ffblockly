#!/usr/bin/env node

const yargs = require('yargs/yargs');
const { hideBin } = require('yargs/helpers');
const fs = require('fs');
const path = require('path');
const sandboxManager = require('./lib/sandbox_manager');
const experimentManager = require('./lib/experiment_manager');

// Helper to load filtergraph JSON
function loadFilterGraph(filePath, jsonString) {
  if (filePath) {
    try {
      const rawData = fs.readFileSync(path.resolve(filePath), 'utf8');
      return JSON.parse(rawData);
    } catch (err) {
      console.error(`Error reading filtergraph file ${filePath}:`, err.message);
      process.exit(1);
    }
  }
  if (jsonString) {
    try {
      return JSON.parse(jsonString);
    } catch (err) {
      console.error('Error parsing filtergraph JSON string:', err.message);
      process.exit(1);
    }
  }
  // Default empty filtergraph
  return { filters: [], connections: [] };
}

yargs(hideBin(process.argv))
  .command(
    'create <sandboxName>',
    'Create a new ffmpeg sandbox/experiment',
    (yargs) => {
      return yargs
        .positional('sandboxName', {
          describe: 'Name for the new sandbox (will be sanitized for directory creation)',
          type: 'string',
        })
        .option('filtergraph-file', {
          alias: 'f',
          type: 'string',
          description: 'Path to a JSON file containing the filtergraph definition.',
        })
        .option('filtergraph-json', {
          alias: 'j',
          type: 'string',
          description: 'A JSON string directly representing the filtergraph.',
        })
        .option('description', {
          alias: 'd',
          type: 'string',
          description: 'A description for the experiment.',
          default: '',
        })
        .option('name', {
          alias: 'n',
          type: 'string',
          description: 'A user-friendly name for the experiment.',
        });
    },
    (argv) => {
      const { sandboxName, filtergraphFile, filtergraphJson, description } = argv;
      const friendlyName = argv.name || sandboxName; // Use sandboxName if friendly name not provided

      const filterGraphObject = loadFilterGraph(filtergraphFile, filtergraphJson);

      // Sanitize sandboxName for directory creation (sandboxManager should ideally handle this internally or expose sanitizer)
      // For now, let's assume sandboxManager.createSandbox handles sanitization or the name is used as is.
      // The sandbox_manager.js created earlier has a sanitizeSandboxName function but doesn't export it.
      // Let's rely on the manager's internal handling.

      const createResult = sandboxManager.createSandbox(sandboxName, filterGraphObject);

      if (createResult.success) {
        console.log(`Sandbox '${sandboxName}' created successfully at ${createResult.path}.`);
        const metadata = {
          name: friendlyName,
          description: description,
          createdAt: new Date().toISOString(),
          status: 'experimental', // Default status
        };
        const metaResult = experimentManager.setExperimentMetadata(sandboxName, metadata);
        if (metaResult.success) {
          console.log(`Initial metadata set for '${sandboxName}'.`);
        } else {
          console.warn(`Warning: Sandbox created, but failed to set initial metadata: ${metaResult.error}`);
        }
      } else {
        console.error(`Error creating sandbox '${sandboxName}': ${createResult.error}`);
      }
    }
  )
  .command(
    'list',
    'List all available sandboxes',
    () => {}, // No specific options for list
    async () => { // Made async to await metadata
      const listResult = sandboxManager.listSandboxes();

      if (Array.isArray(listResult)) { // Assuming listSandboxes returns array on success
        if (listResult.length === 0) {
          console.log('No sandboxes found.');
          return;
        }
        console.log('Available sandboxes:');
        for (const sbName of listResult) {
          const metaResult = experimentManager.getExperimentMetadata(sbName);
          if (metaResult.success) {
            console.log(`  - ${sbName} (Name: "${metaResult.metadata.name}", Status: ${metaResult.metadata.status})`);
          } else {
            console.log(`  - ${sbName} (Could not retrieve metadata: ${metaResult.error})`);
          }
        }
      } else { // Handle potential error object from listSandboxes
          console.error(`Error listing sandboxes: ${listResult.error || 'Unknown error'}`);
      }
    }
  )
  .command(
    'show <sandboxName>',
    'Show filtergraph and metadata for a sandbox',
    (yargs) => {
      return yargs.positional('sandboxName', {
        describe: 'Name of the sandbox to show',
        type: 'string',
      });
    },
    (argv) => {
      const { sandboxName } = argv;
      console.log(`Details for sandbox: ${sandboxName}\n`);

      const fgResult = sandboxManager.getSandboxFilterGraph(sandboxName);
      if (fgResult.success) {
        console.log('Filtergraph:');
        console.log(JSON.stringify(fgResult.filterGraph, null, 2));
      } else {
        console.error(`Error retrieving filtergraph: ${fgResult.error}`);
      }

      console.log('\nMetadata:');
      const metaResult = experimentManager.getExperimentMetadata(sandboxName);
      if (metaResult.success) {
        console.log(JSON.stringify(metaResult.metadata, null, 2));
      } else {
        console.error(`Error retrieving metadata: ${metaResult.error}`);
      }
    }
  )
  .command(
    'run <sandboxName>',
    'Generate and display the ffmpeg command for an experiment',
    (yargs) => {
      return yargs
        .positional('sandboxName', {
          describe: 'Name of the sandbox to run',
          type: 'string',
        })
        .option('input', {
          alias: 'i',
          type: 'string',
          default: 'input.mp4',
          description: 'Input file path for ffmpeg.',
        })
        .option('output', {
          alias: 'o',
          type: 'string',
          // Defaulting output to be inside the sandbox could be:
          // defaultDescription: 'output.mp4 in sandbox directory',
          // Actual default path needs to be constructed in handler if dynamic.
          description: 'Output file path for ffmpeg. If not absolute, relative to current dir.',
        })
        .option('ffmpeg-path', {
          type: 'string',
          default: 'ffmpeg',
          description: 'Path to the ffmpeg executable.',
        });
    },
    (argv) => {
      const { sandboxName, input, ffmpegPath } = argv;
      // Construct default output path if not provided, e.g., inside the sandbox
      const sandboxDir = path.join(sandboxManager.SANDBOXES_DIR, sandboxName);
      const output = argv.output || path.join(sandboxDir, `${sandboxName}_output.mp4`);


      const runResult = experimentManager.runExperiment(sandboxName, ffmpegPath, input, output);
      if (runResult.success) {
        console.log('Conceptual FFmpeg command generated:');
        console.log(runResult.command);
        console.log(`\nInput: ${input}`);
        console.log(`Output: ${output}`);
        console.log(`\nMetadata for '${sandboxName}' should now reflect this run.`);
      } else {
        console.error(`Error running experiment '${sandboxName}': ${runResult.error}`);
      }
    }
  )
  .command(
    'status <sandboxName> <newStatus>',
    "Update the status of an experiment (e.g., 'keep', 'archived')",
    (yargs) => {
      return yargs
        .positional('sandboxName', {
          describe: 'Name of the sandbox',
          type: 'string',
        })
        .positional('newStatus', {
          describe: 'The new status to set',
          type: 'string',
        });
    },
    (argv) => {
      const { sandboxName, newStatus } = argv;
      const statusResult = experimentManager.updateExperimentStatus(sandboxName, newStatus);
      if (statusResult.success) {
        console.log(`Status for sandbox '${sandboxName}' updated to '${newStatus}'.`);
      } else {
        console.error(`Error updating status for '${sandboxName}': ${statusResult.error}`);
      }
    }
  )
  .command(
    'delete <sandboxName>',
    'Delete (flush) an experiment/sandbox',
    (yargs) => {
      return yargs
        .positional('sandboxName', {
          describe: 'Name of the sandbox to delete',
          type: 'string',
        })
        .option('force', { // As per instructions, no interactive confirm for this subtask
          type: 'boolean',
          default: false,
          description: 'Bypass confirmation (not implemented interactively).',
        });
    },
    (argv) => {
      // if (!argv.force) {
      //   console.log(`Run with --force to delete. Interactive confirmation not yet implemented.`);
      //   return;
      // }
      const { sandboxName } = argv;
      const deleteResult = sandboxManager.deleteSandbox(sandboxName);
      if (deleteResult.success) {
        console.log(`Sandbox '${sandboxName}' deleted successfully.`);
      } else {
        console.error(`Error deleting sandbox '${sandboxName}': ${deleteResult.error}`);
      }
    }
  )
  .alias({
    'create': 'c',
    'list': 'ls',
    'show': 's',
    'run': 'r',
    'delete': ['del', 'rm']
  })
  .demandCommand(1, 'You need to specify a command.')
  .help()
  .alias('help', 'h')
  .epilogue('For more information, find the project documentation.')
  .strict() // Catches unrecognized commands/options
  .wrap(yargs.terminalWidth()) // Responsive width
  .parse(); // This is equivalent to .argv but preferred in some docs for clarity with parse()
