package br.com.paulorosado.todolist.task;

import br.com.paulorosado.todolist.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    public ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {

        var userId = request.getAttribute("userId");
        taskModel.setUserId((UUID) userId);

        var currentDate = LocalDateTime.now();

        if (currentDate.isAfter((taskModel.getStartAt())) || currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início e de término não podem ser anteriores a data atual");
        }

        if (taskModel.getEndAt().isBefore((taskModel.getStartAt()))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de teŕmino não pode ser anterior a data de início");
        }

        var task = this.taskRepository.save(taskModel);

        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        var userId = request.getAttribute("userId");
        return this.taskRepository.findByUserId((UUID) userId);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {

        var taskInDb = this.taskRepository.findById(id).orElse(null);

        if (taskInDb == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Tarefa não encontrada");
        }

        var userId = request.getAttribute("userId");

        if (!taskInDb.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Usuário não tem permissão para alterar essa tarefa");
        }

        Utils.copyNonNullProperties(taskModel, taskInDb);

        var updatedTask = this.taskRepository.save(taskInDb);

        return ResponseEntity.ok()
                .body(updatedTask);

    }

}
