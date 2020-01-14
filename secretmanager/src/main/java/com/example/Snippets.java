/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import java.io.IOException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;

public class Snippets {
  public static void main(String... args) throws IOException {
    Snippets snippets = new Snippets();
    CmdLineParser parser = new CmdLineParser(snippets);

    try {
      parser.parseArgument(args);

      if (snippets.help) {
        parser.printUsage(System.err);
        return;
      }

    } catch (CmdLineException e) {
      System.out.println(e);
      System.out.println();
      parser.printUsage(System.err);
      System.exit(1);
    }
    snippets.command.run();
  }

  @Option(name = "-help", aliases = "-h", usage = "Show command line help.", help = true)
  private boolean help;

  interface Command {
    void run() throws IOException;
  }

  static class ProjectIdArgs {
    @Argument(metaVar = "projectId", required = true, index = 0, usage = "GCP project ID")
    String projectId;
  }

  static class SecretIdArgs extends ProjectIdArgs {
    @Argument(metaVar = "secretId", required = true, index = 1, usage = "The secret id")
    String secretId;
  }

  static class VersionIdArgs extends SecretIdArgs {
    @Argument(metaVar = "versionId", required = true, index = 2, usage = "The secret version id")
    String versionId;
  }

  public static class AccessSecretVersionCommand extends VersionIdArgs implements Command {
    public void run() throws IOException {
      new AccessSecretVersion().accessSecretVersion(projectId, secretId, versionId);
    }
  }

  public static class AddSecretVersionCommand extends SecretIdArgs implements Command {
    public void run() throws IOException {
      new AddSecretVersion().addSecretVersion(projectId, secretId);
    }
  }

  public static class CreateSecretCommand extends SecretIdArgs implements Command {
    public void run() throws IOException {
      new CreateSecret().createSecret(projectId, secretId);
    }
  }

  public static class DeleteSecretCommand extends SecretIdArgs implements Command {
    public void run() throws IOException {
      new DeleteSecret().deleteSecret(projectId, secretId);
    }
  }

  public static class DestroySecretVersionCommand extends VersionIdArgs implements Command {
    public void run() throws IOException {
      new DestroySecretVersion().destroySecretVersion(projectId, secretId, versionId);
    }
  }

  public static class DisableSecretVersionCommand extends VersionIdArgs implements Command {
    public void run() throws IOException {
      new DisableSecretVersion().disableSecretVersion(projectId, secretId, versionId);
    }
  }

  public static class EnableSecretVersionCommand extends VersionIdArgs implements Command {
    public void run() throws IOException {
      new EnableSecretVersion().enableSecretVersion(projectId, secretId, versionId);
    }
  }

  public static class GetSecretCommand extends SecretIdArgs implements Command {
    public void run() throws IOException {
      new GetSecret().getSecret(projectId, secretId);
    }
  }

  public static class GetSecretVersionCommand extends VersionIdArgs implements Command {
    public void run() throws IOException {
      new GetSecretVersion().getSecretVersion(projectId, secretId, versionId);
    }
  }

  public static class ListSecretsCommand extends ProjectIdArgs implements Command {
    public void run() throws IOException {
      new ListSecrets().listSecrets(projectId);
    }
  }

  public static class ListSecretVersionsCommand extends SecretIdArgs implements Command {
    public void run() throws IOException {
      new ListSecretVersions().listSecretVersions(projectId, secretId);
    }
  }

  public static class UpdateSecretCommand extends SecretIdArgs implements Command {
    public void run() throws IOException {
      new UpdateSecret().updateSecret(projectId, secretId);
    }
  }

  @Argument(
      metaVar = "command",
      required = true,
      handler = SubCommandHandler.class,
      usage = "The snippet to run")
  @SubCommands({
      @SubCommand(name = "accessSecretVersion", impl = AccessSecretVersionCommand.class),
      @SubCommand(name = "addSecretVersion", impl = AddSecretVersionCommand.class),
      @SubCommand(name = "createSecret", impl = CreateSecretCommand.class),
      @SubCommand(name = "deleteSecret", impl = DeleteSecretCommand.class),
      @SubCommand(name = "destroySecretVersion", impl = DestroySecretVersionCommand.class),
      @SubCommand(name = "disableSecretVersion", impl = DisableSecretVersionCommand.class),
      @SubCommand(name = "enableSecretVersion", impl = EnableSecretVersionCommand.class),
      @SubCommand(name = "getSecret", impl = GetSecretCommand.class),
      @SubCommand(name = "getSecretVersion", impl = GetSecretVersionCommand.class),
      @SubCommand(name = "listSecrets", impl = ListSecretsCommand.class),
      @SubCommand(name = "listSecretVersions", impl = ListSecretVersionsCommand.class),
      @SubCommand(name = "updateSecret", impl = UpdateSecretCommand.class)
  })
  Command command;
}
