FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

# Remove broken yarn repo that causes GPG error
RUN rm -f /etc/apt/sources.list.d/yarn.list

# Install tools
RUN apt-get update && apt-get install -y \
    maven \
    git \
    curl \
    wget \
    unzip \
    nano \
    ca-certificates \
    gnupg \
    lsb-release \
    && rm -rf /var/lib/apt/lists/*

# Install GitHub CLI
RUN curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg \
  | dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg \
  && chmod go+r /usr/share/keyrings/githubcli-archive-keyring.gpg \
  && echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" \
  | tee /etc/apt/sources.list.d/github-cli.list > /dev/null \
  && apt-get update \
  && apt-get install gh -y

# Install Supabase CLI
RUN curl -fsSL https://supabase.com/install.sh | sh

WORKDIR /workspace

RUN java -version && mvn -version && git --version