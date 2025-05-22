# QA Middleware Messaging

This project provides messaging components including **producers** and **subscribers** for two major messaging technologies:

- **Solace**
- **Tibco Rendezvous (TibRv)**

These components are designed to support message publishing and consumption in a QA or integration testing context.

---

## 📁 Project Structure

- `/solace`: Contains Solace-specific messaging implementations.
- `/tibrv`: Contains Tibco Rendezvous-specific messaging implementations.
- `build.gradle`, `settings.gradle`: Gradle build configuration files.
- `pipeline.yaml`: CI/CD pipeline definition.
- `README.md`: This file.

---

## 📦 Modules

Each module includes its own implementation of message producers and subscribers, along with custom exceptions and utility classes. Please refer to the **README inside each module** for detailed instructions on usage:

- [Solace Module README](./solace/README.md)
- [TibRv Module README](./tibrv/README.md)

---

## 🛠️ Getting Started

This is a Gradle-based project. To build the project:

```bash
./gradlew build
