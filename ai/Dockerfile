FROM vika/ai-server-base

WORKDIR /app
COPY ./.version /app/.version
# main codes
COPY ./databus_client /app/databus_client
COPY ./ai_shared /app/ai_shared
COPY ./ai_inference /app/ai_inference
COPY ./ai_trainers /app/ai_trainers
COPY ./ai_server /app/ai_server
COPY ./ai_copilot /app/ai_copilot

COPY ./scripts/docker_run.sh /app/docker_run.sh

CMD ["bash", "./docker_run.sh"]

# persistent vector db will store in `/app/ai_server/.data`