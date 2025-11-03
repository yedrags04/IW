
up:
	@docker-compose down -v --remove-orphans
	@docker-compose up --build

rapido:
	@docker-compose down
	@docker-compose up -d

# 🔍 Ver logs en vivo
logs:
	@docker-compose logs -f

clean:
	@docker system prune -a --volumes -f
