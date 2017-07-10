package com.kshrd.spring.controller.restful;

import com.kshrd.spring.entity.User;
import com.kshrd.spring.response.*;
import com.kshrd.spring.response.failure.ResponseListFailure;
import com.kshrd.spring.response.failure.ResponseRecordFailure;
import com.kshrd.spring.service.UserService;
import com.kshrd.spring.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/v1/api/user")
public class UserRestControllerV1 {

	UserService userService;
	RoleService userRoleService;

	HttpStatus httpStatus;
	
	@Autowired
	public UserRestControllerV1(UserService userService, RoleService userRoleService) {
		this.userService = userService;
		this.userRoleService = userRoleService;
	}

	@GetMapping("/find-all-users")
	public ResponseEntity<ResponseList<User>> findAllUsers(@RequestParam("page") int page, @RequestParam("limit") int limit){
		ResponseList<User> responseList = new ResponseList<>();
		
		try{

			httpStatus = HttpStatus.OK;
			List<User> users = userService.findAllUsers();
			
			Pagination pagination = new Pagination();
			pagination.setLimit(limit);
			pagination.setPage(page);
			pagination.setTotalCount(users.size());
			pagination.setOffset((page - 1) * limit);
			System.out.println("Offset is " + pagination.getOffset());
			
			if(!users.isEmpty()){
				responseList = new ResponseList<User>(HttpMessage.success(Table.USERS, Transaction.Success.RETRIEVE),
													true, users, pagination);
			} else {
				httpStatus = HttpStatus.NOT_FOUND;
				responseList = new ResponseListFailure<>(HttpMessage.fail(Table.USERS, Transaction.Fail.RETRIEVE), false,
								ResponseHttpStatus.NOT_FOUND);
			}
		}catch(Exception e){
			e.printStackTrace();
			httpStatus = httpStatus.INTERNAL_SERVER_ERROR;
			responseList = new ResponseListFailure<>(HttpMessage.fail(Table.USERS, Transaction.Fail.RETRIEVE), false, 
							ResponseHttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<ResponseList<User>>(responseList, httpStatus);
	}

	@GetMapping("/{uuid}")
	public ResponseEntity<ResponseRecord<User>> findUserByUUID(@PathVariable("uuid") String uuid){
		ResponseRecord<User> responseRecord = new ResponseRecord<>();
		try{
			User user = userService.findUserByUUID(uuid);
			if(user!=null){
				responseRecord = new ResponseRecord<User>(HttpMessage.success(Table.USERS,
															Transaction.Success.RETRIEVE), true, user);
			}
			else{
				httpStatus = HttpStatus.NOT_FOUND;
				responseRecord = new ResponseRecordFailure<User>(HttpMessage.fail(Table.USERS,
															Transaction.Fail.RETRIEVE), false, ResponseHttpStatus.NOT_FOUND);
			}
		}catch(Exception e){
			e.printStackTrace();
			httpStatus = httpStatus.INTERNAL_SERVER_ERROR;
			responseRecord = new ResponseRecordFailure<User>(HttpMessage.fail(Table.USERS, Transaction.Fail.RETRIEVE),
																		false, ResponseHttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<ResponseRecord<User>>(responseRecord, httpStatus);
	}

	@PutMapping("/update")
	public ResponseEntity<ResponseRecord<User>> updateUser(@RequestBody User user){
		ResponseRecord<User> responseRecord = null;
		try{
			if(userService.updateUser(user)){
				userRoleService.deleteUserRoleByUserId(user.getUuid());
				userRoleService.insertUserRole(user.getRoles(), userService.getUserIDByUUID(user.getUuid()));
				responseRecord = new ResponseRecord<>(HttpMessage.success(Table.USER_ROLES, Transaction.Success.UPDATED),
																		true, userService.findUserByUUID(user.getUuid()));
			} else {
				httpStatus = HttpStatus.NOT_FOUND;
				responseRecord = new ResponseRecordFailure<>(HttpMessage.fail(Table.USERS, Transaction.Fail.UPDATED),  
																				false, ResponseHttpStatus.NOT_FOUND);
			}				
			
		}catch(Exception e){
			e.printStackTrace();
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseRecord = new ResponseRecordFailure<>(HttpMessage.fail(Table.USER_ROLES, Transaction.Fail.UPDATED), 
														 true, ResponseHttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<ResponseRecord<User>>(responseRecord, httpStatus);
	}

	@PutMapping("/status/{uuid}/{status}")
	public ResponseEntity<ResponseRecord<User>> updateUserStatusByUUID(@PathVariable("uuid") String uuid,
																	   @PathVariable("status") String status){
		ResponseRecord<User> responseRecord = null;
		try{
			if(userService.updateUserStatusByUUID(uuid, status)){
				responseRecord = new ResponseRecord<>(HttpMessage.success(Table.USER_ROLES, Transaction.Success.UPDATED), 
						 													true, userService.findUserByUUID(uuid));
			} else {
				httpStatus = HttpStatus.NOT_FOUND;
				responseRecord = new ResponseRecordFailure<>(HttpMessage.fail(Table.USERS, Transaction.Fail.UPDATED),  
																				false, ResponseHttpStatus.NOT_FOUND);
			}
		}catch(Exception e){
			e.printStackTrace();
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseRecord = new ResponseRecordFailure<>(HttpMessage.fail(Table.USER_ROLES, Transaction.Fail.UPDATED), 
														 					true, ResponseHttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<ResponseRecord<User>>(responseRecord, httpStatus);
	}

	@GetMapping("/delete/{uuid}")
	public ResponseEntity<ResponseRecord<User>> deleteUserByUUID(@PathVariable("uuid") String uuid){
		ResponseRecord<User> responseRecord = null;
		try{
			User user = userService.findUserByUUID(uuid);
			userRoleService.deleteUserRoleByUserId(user.getUuid());
			if(userService.deleteUserByUUID(uuid)){
				responseRecord = new ResponseRecord<>(HttpMessage.success(Table.USER_ROLES, Transaction.Success.DELETED),
																			true, user);
			} else {
				httpStatus = HttpStatus.NOT_FOUND;
				responseRecord = new ResponseRecordFailure<>(HttpMessage.fail(Table.USERS, Transaction.Fail.DELETED),  
																			false, ResponseHttpStatus.NOT_FOUND);
			}				
			
		}catch(Exception e){
			e.printStackTrace();
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseRecord = new ResponseRecordFailure<>(HttpMessage.fail(Table.USER_ROLES, Transaction.Fail.DELETED), 
														 				true, ResponseHttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<ResponseRecord<User>>(responseRecord, httpStatus);
	}	
	
	@PostMapping
	public ResponseEntity<ResponseRecord<User>> insertUser(@RequestBody User user){
		ResponseRecord<User> responseRecord = null;
		try{
			httpStatus = HttpStatus.OK;
			if(userService.insertUser(user)){
				userRoleService.insertUserRole(user.getRoles(), userService.getUserIDByUUID(user.getUuid()));
				responseRecord = new ResponseRecord<>(HttpMessage.success(Table.USERS, Transaction.Success.CREATED), 
						 												true, userService.findUserByUUID(user.getUuid()));
			}
			else{
				httpStatus = HttpStatus.BAD_REQUEST;
				responseRecord = new ResponseRecordFailure<>(HttpMessage.fail(Table.USERS, Transaction.Fail.CREATED),  
																				false, ResponseHttpStatus.BAD_REQUEST);
			}				
			
		}catch(Exception e){
			e.printStackTrace();
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseRecord = new ResponseRecordFailure<>(HttpMessage.fail(Table.USERS, Transaction.Fail.CREATED), 
														 				true, ResponseHttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<ResponseRecord<User>>(responseRecord, httpStatus);
	}
	
	@PostMapping("/find-user-by-email")
	public ResponseEntity<ResponseRecord<User>> findUserByEmail(@RequestParam("email") String email){
		ResponseRecord<User> responseRecord = null;
		try{
			User myUser = userService.findUserByEmail(email);
			if(myUser != null){
				responseRecord = new ResponseRecord<>(HttpMessage.success(Table.USER_ROLES, Transaction.Success.RETRIEVE), 
						 													true, myUser);
			}
			else{
				httpStatus = HttpStatus.NOT_FOUND;
				responseRecord = new ResponseRecordFailure<>(HttpMessage.fail(Table.USERS, Transaction.Fail.RETRIEVE),  
																				false, ResponseHttpStatus.NOT_FOUND);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			responseRecord = new ResponseRecordFailure<>(HttpMessage.fail(Table.USER_ROLES, Transaction.Fail.RETRIEVE), 
														 				true, ResponseHttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<ResponseRecord<User>>(responseRecord, httpStatus);
	}
	
}