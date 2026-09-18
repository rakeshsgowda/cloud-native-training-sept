FROM node:24-slim

WORKDIR /app

RUN npm install -g serve
COPY dist retailweb

EXPOSE 7080
ENTRYPOINT ["npx","serve", "-s", "retailweb", "-l", "7080"]